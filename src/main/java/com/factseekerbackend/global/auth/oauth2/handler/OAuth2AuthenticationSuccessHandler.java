package com.factseekerbackend.global.auth.oauth2.handler;

import com.factseekerbackend.global.auth.jwt.JwtTokenProvider;
import com.factseekerbackend.global.auth.oauth2.CustomOAuth2User;
import com.factseekerbackend.global.auth.oauth2.repository.OAuth2AuthorizationRequestRepository;
import com.factseekerbackend.global.auth.oauth2.util.CookieUtils;
import com.factseekerbackend.global.exception.BusinessException;
import com.factseekerbackend.global.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

  @Value("${oauth2.authorized-redirect-uris}")
  private String[] authorizedRedirectUris;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {

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

    Optional<String> redirectUri = CookieUtils.getCookie(request, "redirect_uri")
        .map(Cookie::getValue);

    if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
      throw new BusinessException(ErrorCode.OAUTH2_AUTHENTICATION_PROCESSING_ERROR,
          "승인되지 않은 Redirect URI입니다.");
    }

    String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

    Authentication jwtauthentication = new UsernamePasswordAuthenticationToken(
        String.valueOf(oAuth2User.getId()), null, authentication.getAuthorities());

    String accessToken = jwtTokenProvider.createAccessToken(jwtauthentication);
    String refreshToken = jwtTokenProvider.createRefreshToken(jwtauthentication);

    return UriComponentsBuilder.fromUriString(targetUrl)
        .queryParam("token", accessToken)
        .queryParam("refreshToken", refreshToken)
        .build().toUriString();
  }

  protected void clearAuthenticationAttributes(HttpServletRequest request,
      HttpServletResponse response) {
    super.clearAuthenticationAttributes(request);
    authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
  }

  private boolean isAuthorizedRedirectUri(String uri) {
    URI clientRedirectUri = URI.create(uri);

    for (String authorizedRedirectUri : authorizedRedirectUris) {
      URI authorizedURI = URI.create(authorizedRedirectUri);

      if (authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
          && authorizedURI.getPort() == clientRedirectUri.getPort()) {
        return true;
      }
    }
    return false;
  }

}
