package com.factseekerbackend.global.config;

import com.factseekerbackend.global.auth.oauth2.repository.OAuth2AuthorizationRequestRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OAuth2Config {

  @Bean
  public OAuth2AuthorizationRequestRepository oAuth2AuthorizationRequestRepository() {
    return new OAuth2AuthorizationRequestRepository();
  }
}
