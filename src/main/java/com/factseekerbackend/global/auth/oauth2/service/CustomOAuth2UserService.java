package com.factseekerbackend.global.auth.oauth2.service;

import com.factseekerbackend.domain.user.entity.AuthProvider;
import com.factseekerbackend.domain.user.entity.Role;
import com.factseekerbackend.domain.user.entity.User;
import com.factseekerbackend.domain.user.repository.UserRepository;
import com.factseekerbackend.global.auth.oauth2.CustomOAuth2User;
import com.factseekerbackend.global.auth.oauth2.user.OAuth2UserInfo;
import com.factseekerbackend.global.auth.oauth2.user.OAuth2UserInfoFactory;
import com.factseekerbackend.global.exception.BusinessException;
import com.factseekerbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest)
      throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

    try {
      return processOAuth2User(oAuth2UserRequest, oAuth2User);
    } catch (Exception e) {
      log.error("OAuth2 사용자 처리 중 오류 발생", e);
      throw new OAuth2AuthenticationException("OAutn2 사용자 처리 중 오류가 발생했습니다.");
    }
  }

  private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
    String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();

    OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId,
        oAuth2User.getAttributes());

    if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
      throw new BusinessException(ErrorCode.OAUTH2_AUTHENTICATION_PROCESSING_ERROR,
          "소셜 로그인에서 이메일을 찾을 수 없습니다.");
    }

    User user = userRepository.findByEmail(oAuth2UserInfo.getEmail())
        .map(existingUser -> updateExistingUser(existingUser, oAuth2UserInfo, registrationId))
        .orElseGet(() -> registerNewUser(oAuth2UserInfo, registrationId));

    return new CustomOAuth2User(
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        user.getRole(),
        oAuth2User.getAttributes()
    );
  }

  private User registerNewUser(OAuth2UserInfo oAuth2UserInfo, String registrationId) {
    log.info("새로운 OAuth2 사용자 등록: {}", oAuth2UserInfo.getEmail());

    AuthProvider authProvider = AuthProvider.valueOf(registrationId.toUpperCase());

    User user = User.builder()
        .email(oAuth2UserInfo.getEmail())
        .fullName(oAuth2UserInfo.getName())
        .socialId(oAuth2UserInfo.getId())
        .authProvider(authProvider)
        .role(Role.USER)
        .emailVerified(true)
        .build();

    return userRepository.save(user);
  }

  private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo,
      String registrationId) {
    log.info("기존 OAuth2 사용자 정보 업데이트: {}", oAuth2UserInfo.getEmail());

    if (existingUser.getAuthProvider() == null
        || existingUser.getAuthProvider() == AuthProvider.LOCAL) {
      AuthProvider authProvider = AuthProvider.valueOf(registrationId.toUpperCase());
      User updatedUser = User.builder()
          .id(existingUser.getId())
          .loginId(existingUser.getLoginId())
          .password(existingUser.getPassword())
          .fullName(existingUser.getFullName())
          .phone(existingUser.getPhone())
          .email(existingUser.getEmail())
          .role(existingUser.getRole())
          .authProvider(authProvider)
          .socialId(oAuth2UserInfo.getId())
          .emailVerified(true)
          .build();

      return userRepository.save(updatedUser);
    }
    existingUser.updateProfile(oAuth2UserInfo.getName());
    return userRepository.save(existingUser);
  }

}
