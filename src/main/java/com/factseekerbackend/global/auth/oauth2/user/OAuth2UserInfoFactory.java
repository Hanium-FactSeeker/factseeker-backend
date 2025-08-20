package com.factseekerbackend.global.auth.oauth2.user;

import com.factseekerbackend.domain.user.entity.AuthProvider;
import com.factseekerbackend.global.exception.BusinessException;
import com.factseekerbackend.global.exception.ErrorCode;
import java.util.Map;

public class OAuth2UserInfoFactory {

  public static OAuth2UserInfo getOAuth2UserInfo(String registrationId,
      Map<String, Object> attributes) {
    if (registrationId.equalsIgnoreCase(AuthProvider.KAKAO.toString())) {
      return new KakaoOAuth2UserInfo(attributes);
    } else if (registrationId.equalsIgnoreCase(AuthProvider.NAVER.toString())) {
      return new NaverOAuth2UserInfo(attributes);
    } else {
      throw new BusinessException(ErrorCode.OAUTH2_AUTHENTICATION_PROCESSING_ERROR,
          "지원하지 않는 로그인 방식입니다." + registrationId);
    }
  }
}
