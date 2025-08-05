package com.factseekerbackend.global.auth.oauth2.service;

import com.factseekerbackend.domain.user.entity.AuthProvider;
import com.factseekerbackend.domain.user.entity.User;
import com.factseekerbackend.domain.user.repository.UserRepository;
import com.factseekerbackend.global.auth.jwt.JwtTokenProvider;
import com.factseekerbackend.global.auth.oauth2.CustomOAuth2User;
import com.factseekerbackend.global.auth.oauth2.dto.SocialUserInfo;
import com.factseekerbackend.global.auth.oauth2.user.OAuth2UserInfo;
import com.factseekerbackend.global.auth.oauth2.user.OAuth2UserInfoFactory;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final RedisTemplate<String, Object> redisTemplate;

  private static final String SOCIAL_TEMP_PREFIX = "social_temp:";
  private static final Duration SOCIAL_TEMP_EXPIRY = Duration.ofMinutes(15);

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);
    String registrationId = userRequest.getClientRegistration().getRegistrationId();

    OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId,
        oAuth2User.getAttributes());

    Optional<User> userOptional = userRepository.findByLoginId(oAuth2UserInfo.getLoginId());

    boolean isNewUser = userOptional.isEmpty();
    Map<String, Object> finalAttributes = new HashMap<>(oAuth2User.getAttributes());
    Set<GrantedAuthority> authorities;

    if (isNewUser) {
      // 신규 사용자
      log.info("신규 사용자: 회원가입 절차 진행. loginId: {}", oAuth2UserInfo.getLoginId());

      // 1. 임시 토큰 생성
      String tempToken = jwtTokenProvider.createTempToken(oAuth2UserInfo.getLoginId());
      finalAttributes.put("tempToken", tempToken);

      SocialUserInfo socialUserInfo = SocialUserInfo.builder()
          .providerId(oAuth2UserInfo.getId())
          .provider(AuthProvider.valueOf(registrationId.toUpperCase()))
          .email(oAuth2UserInfo.getEmail())
          .name(oAuth2UserInfo.getName())
          .tempToken(tempToken)
          .build();

      redisTemplate.opsForValue().set(SOCIAL_TEMP_PREFIX + tempToken, socialUserInfo, SOCIAL_TEMP_EXPIRY);

      authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_PRE_SOCIAL_SIGNUP"));
    } else {
      // 기존 사용자
      User existingUser = userOptional.get();
      log.info("기존 사용자 로그인: {}", existingUser.getLoginId());

      authorities = existingUser.getRoles().stream()
          .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
          .collect(Collectors.toSet());
    }

    return CustomOAuth2User.builder()
        .attributes(finalAttributes)
        .nameAttributeKey(
            userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
                .getUserNameAttributeName())
        .authorities(authorities)
        .loginId(oAuth2UserInfo.getLoginId())
        .isNewUser(isNewUser)
        .build();
  }

}