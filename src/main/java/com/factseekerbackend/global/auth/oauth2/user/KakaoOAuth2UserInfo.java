package com.factseekerbackend.global.auth.oauth2.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoOAuth2UserInfo extends OAuth2UserInfo {

  private String loginId;

  public KakaoOAuth2UserInfo() {
    super();
  }

  public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
    super(attributes);
  }

  @Override
  public String getId() {
    return String.valueOf(attributes.get("id"));
  }

  @Override
  public String getName() {
    Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
    if (properties == null) {
      return null;
    }
    return (String) properties.get("nickname");
  }

  @Override
  public String getEmail() {
    Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
    if (kakaoAccount == null) {
      return null;
    }
    return (String) kakaoAccount.get("email");
  }

  @Override
  public String getLoginId() {
    return "kakao_" + getId();
  }

}
