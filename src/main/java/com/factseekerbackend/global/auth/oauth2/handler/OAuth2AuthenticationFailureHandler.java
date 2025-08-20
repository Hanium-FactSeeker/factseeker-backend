package com.factseekerbackend.global.auth.oauth2.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.naming.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

  @Value("${oauth2.authorized-redirect-uris}")
  private String clientUrl;

  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {

    String targetUrl = UriComponentsBuilder.fromUriString(clientUrl + "/oauth2/error")
        .queryParam("error", exception.getLocalizedMessage())
        .build().toUriString();

    log.error("OAuth2 로그인 실패: {}", exception.getMessage());

    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

}
