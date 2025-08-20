package com.factseekerbackend.global.auth.oauth2.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverOAuth2UserInfo extends OAuth2UserInfo {

  private String loginId;

  public NaverOAuth2UserInfo() {
    super();
  }

  public NaverOAuth2UserInfo(Map<String, Object> attributes) {
    super((Map<String, Object>) attributes.get("response"));
  }

  @Override
  public String getId() {
    return (String) attributes.get("id");
  }

  @Override
  public String getName() {
    return (String) attributes.get("name");
  }

  @Override
  public String getEmail() {
    return (String) attributes.get("email");
  }

  @Override
  public String getLoginId() {
    return "naver_" + getId();
  }

}
