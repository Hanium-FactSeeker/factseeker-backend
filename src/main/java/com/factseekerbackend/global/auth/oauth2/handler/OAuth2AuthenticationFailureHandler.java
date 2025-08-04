package com.factseekerbackend.global.auth.oauth2.handler;

import com.factseekerbackend.global.auth.oauth2.repository.OAuth2AuthorizationRequestRepository;
import com.factseekerbackend.global.auth.oauth2.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.naming.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

  private final OAuth2AuthorizationRequestRepository authorizationRequestRepository;

  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
    String targetUrl = CookieUtils.getCookie(request, "redirect_uri")
        .map(Cookie::getValue)
        .orElse("/");

    log.error("OAuth2 인증 실패: {}", exception.getMessage());

    targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
        .queryParam("error", exception.getLocalizedMessage())
        .build().toUriString();

    authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

}
