package com.factseekerbackend.global.auth.oauth2.handler;

import com.factseekerbackend.global.auth.jwt.JwtTokenProvider;
import com.factseekerbackend.global.auth.jwt.service.JwtService;
import com.factseekerbackend.global.auth.oauth2.CustomOAuth2User;
import com.factseekerbackend.global.auth.oauth2.repository.OAuth2AuthorizationRequestRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final OAuth2AuthorizationRequestRepository authorizationRequestRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtService jwtService;

  @Value("${oauth2.authorized-redirect-uris}")
  private String clientUrl;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    log.info("OAuth2AuthenticationSuccessHandler 호출됨");

    String targetUrl = determineTargetUrl(request, response, authentication);

    if (response.isCommitted()) {
      log.debug("Response has already been committed. Unable to redirect to " + targetUrl);
      return;
    }

    clearAuthenticationAttributes(request, response);
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {

    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
    String loginId = oAuth2User.getLoginId();

    if (oAuth2User.isNewUser()) {
      // 신규 사용자
      String tempToken = (String) oAuth2User.getAttributes().get("tempToken");
      return UriComponentsBuilder.fromUriString(clientUrl + "/oauth2/signup")
          .queryParam("token", tempToken)
          .build().toUriString();
    } else {
      // 기존 사용자
      String accessToken = jwtTokenProvider.createAccessToken(authentication);
      String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

      jwtService.storeRefreshToken(loginId, refreshToken);

      return UriComponentsBuilder.fromUriString(clientUrl + "/login/success")
          .queryParam("accessToken", accessToken)
          .queryParam("refreshToken", refreshToken)
          .build().toUriString();
    }
  }

  protected void clearAuthenticationAttributes(HttpServletRequest request,
      HttpServletResponse response) {
    super.clearAuthenticationAttributes(request);
    authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
  }

}
