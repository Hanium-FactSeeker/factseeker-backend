package com.factseekerbackend.global.auth.jwt.service;

import com.factseekerbackend.domain.user.entity.User;
import com.factseekerbackend.domain.user.repository.UserRepository;
import com.factseekerbackend.global.auth.dto.request.SocialUserInfoResponse;
import com.factseekerbackend.global.auth.dto.response.UserInfoResponse;
import com.factseekerbackend.domain.user.entity.CustomUserDetails;
import com.factseekerbackend.global.auth.jwt.JwtTokenProvider;
import com.factseekerbackend.global.auth.jwt.dto.response.TokenRefreshResponse;
import com.factseekerbackend.global.auth.oauth2.dto.SocialUserInfo;
import com.factseekerbackend.domain.user.service.CustomUserDetailsService;
import com.factseekerbackend.global.exception.BusinessException;
import com.factseekerbackend.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

  @Lazy
  private final CustomUserDetailsService customUserDetailsService;
  private final RedisTemplate<String, Object> redisTemplate;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;

  private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
  private static final String BLACKLIST_PREFIX = "blacklist:";
  private static final String REUSED_TOKEN_PREFIX = "reused_token:";
  private static final Duration REFRESH_TOKEN_EXPIRY = Duration.ofDays(7);
  private static final Duration REUSED_TOKEN_EXPIRY = Duration.ofSeconds(10);

  // 소셜 로그인 임시 토큰 검증
  public SocialUserInfoResponse verifySocialToken(String tempToken) {
    Object socialInfoObject = redisTemplate.opsForValue()
        .get("social_temp:" + tempToken);

    if (socialInfoObject == null) {
      throw new IllegalArgumentException("유효하지 않거나 만료된 토큰입니다.");
    }

    SocialUserInfo socialUserInfo = (SocialUserInfo) socialInfoObject;

    return SocialUserInfoResponse.builder()
        .email(socialUserInfo.getEmail())
        .name(socialUserInfo.getName())
        .provider(socialUserInfo.getProvider())
        .build();
  }

  @Transactional
  public String extractTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
        return bearerToken.substring(7).trim();
    } else if (bearerToken == null) {
        return null;
    }

      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
  }

  @Transactional
  public TokenRefreshResponse refreshAccessToken(String refreshToken, String clientIP) {
    try {
      validateRefreshToken(refreshToken);
      String loginId = jwtTokenProvider.getUsernameFromToken(refreshToken);
      validateRefreshTokenSecurity(loginId, refreshToken);

      Authentication authentication = createAuthenticationFromLoginId(loginId);
      TokenPair newTokens = generateTokenPair(authentication);
      rotateRefreshToken(loginId, refreshToken, newTokens.getRefreshToken());

      User user = userRepository.findByLoginId(loginId)
          .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

      logSecurityEvent("TOKEN_REFRESH_SUCCESS", loginId, clientIP);

      return TokenRefreshResponse.builder()
          .accessToken(newTokens.getAccessToken())
          .refreshToken(newTokens.getRefreshToken())
          .tokenType("Bearer")
          .success(true)
          .expiresIn(jwtTokenProvider.getExpiration(newTokens.getAccessToken()))
          .user(UserInfoResponse.from(user))
          .message("토큰 갱신 성공")
          .build();
    } catch (Exception e) {
      logSecurityEvent("TOKEN_REFRESH_FAILED", "unknown", clientIP);
      throw new BusinessException(ErrorCode.TOKEN_REFRESH_FAILED);
    }
  }

  @Transactional
  public void logout(String accessToken, String clientIP) {
    validateAccessToken(accessToken);

    String loginId = jwtTokenProvider.getUsernameFromToken(accessToken);

    removeRefreshToken(loginId);
    blacklistAccessToken(accessToken);
    logSecurityEvent("LOGOUT_SUCCESS", loginId, clientIP);
  }

  @Transactional
  public UserInfoResponse getUserInfo(String accessToken) {
    validateAccessToken(accessToken);

    Claims claims = jwtTokenProvider.getClaims(accessToken);
    String loginId = claims.getSubject();

    User user = userRepository.findByLoginId(loginId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    return UserInfoResponse.from(user);  // User 엔티티로 응답 생성
  }

  @Transactional
  public boolean isTokenBlacklisted(String accessToken) {
    return redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken);
  }

  @Transactional
  public void removeRefreshToken(String loginId) {
    redisTemplate.delete(REFRESH_TOKEN_PREFIX + loginId);
  }

  @Transactional
  public void revokeAllUserTokens(String loginId) {
    removeRefreshToken(loginId);
    logSecurityEvent("ALL_TOKENS_REVOKED", loginId, null);
  }

  @Transactional
  public void revokeAllUserTokens(String loginId, String reason) {
    removeRefreshToken(loginId);
    logSecurityEvent("ALL_TOKENS_REVOKED", loginId, null, reason);
  }

  @Transactional
  public String generateAccessToken(String loginId) {
    Authentication authentication = createAuthenticationFromLoginId(loginId);
    return jwtTokenProvider.createAccessToken(authentication);
  }

  @Transactional
  public String generateRefreshToken(String loginId) {
    Authentication authentication = createAuthenticationFromLoginId(loginId);
    return jwtTokenProvider.createRefreshToken(authentication);
  }

  @Transactional
  public void storeRefreshToken(String loginId, String refreshToken) {
    String key = REFRESH_TOKEN_PREFIX + loginId;
    redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_EXPIRY);
  }

  private TokenPair generateTokenPair(Authentication authentication) {
    String accessToken = jwtTokenProvider.createAccessToken(authentication);
    String refreshToken = jwtTokenProvider.createRefreshToken(authentication);
    return new TokenPair(accessToken, refreshToken);
  }

  private void validateRefreshToken(String refreshAccessToken) {
    if (!jwtTokenProvider.validateRefreshToken(refreshAccessToken)) {
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }
  }

  private void validateAccessToken(String accessToken) {
    if (!jwtTokenProvider.validateAccessToken(accessToken)) {
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }
  }

  private void validateRefreshTokenSecurity(String loginId, String refreshToken) {
    String reusedKey = REUSED_TOKEN_PREFIX + refreshToken;
    if (redisTemplate.hasKey(reusedKey)) {
      log.error("SECURITY_ALERT: Refresh token reuse detected for user: {}", loginId);
      revokeAllUserTokens(loginId, "TOKEN_REUSE_DETECTED");
      throw new SecurityException("토큰 재사용이 감지되었습니다. 보안을 위해 모든 세션이 종료됩니다.");
    }

    Object storedTokenObj = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + loginId);
    if (!(storedTokenObj instanceof String storedToken) || !refreshToken.equals(storedToken)) {
      log.error("SECURITY_ALERT: Invalid refresh token for user: {}", loginId);
      revokeAllUserTokens(loginId, "INVALID_REFRESH_TOKEN");
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }
  }

  private Authentication createAuthenticationFromLoginId(String loginId) {
    CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(
        loginId);
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }

  private void rotateRefreshToken(String loginId, String oldRefreshToken, String newRefreshToken) {
    String reusedKey = REUSED_TOKEN_PREFIX + oldRefreshToken;
    redisTemplate.opsForValue().set(reusedKey, "invalidated", REUSED_TOKEN_EXPIRY);

    storeRefreshToken(loginId, newRefreshToken);
  }

  private List<String> extractRoles(Claims claims) {
    @SuppressWarnings("unchecked")
    List<String> roles = (List<String>) claims.get("roles");
    return roles != null ? roles : List.of();
  }

  private void blacklistAccessToken(String accessToken) {
    Long expiration = jwtTokenProvider.getExpiration(accessToken);
    if (expiration > 0) {
      redisTemplate.opsForValue()
          .set(BLACKLIST_PREFIX + accessToken, "logout", Duration.ofMillis(expiration));
    }
  }

  private void logSecurityEvent(String event, String loginId, String clientIP) {
    logSecurityEvent(event, loginId, clientIP, null);
  }

  private void logSecurityEvent(String event, String loginId, String clientIP, String details) {
    if (details != null) {
      log.info("SECURITY_EVENT: {} - User: {}, IP: {}, Details: {}",
          event, loginId, clientIP, details);
    } else {
      log.info("SECURITY_EVENT: {} - User: {}, IP: {}", event, loginId, clientIP);
    }
  }

  @Data
  @AllArgsConstructor
  private static class TokenPair { // inner class

    private final String accessToken;
    private final String refreshToken;
  }

}
