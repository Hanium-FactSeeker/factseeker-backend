package com.factseekerbackend.global.auth.jwt.service;

import com.factseekerbackend.global.auth.dto.request.LoginRequest;
import com.factseekerbackend.global.auth.dto.request.LoginResponse;
import com.factseekerbackend.global.auth.dto.response.UserInfo;
import com.factseekerbackend.global.auth.jwt.CustomUserDetails;
import com.factseekerbackend.global.auth.jwt.JwtTokenProvider;
import com.factseekerbackend.global.auth.jwt.dto.TokenRefreshResponse;
import com.factseekerbackend.global.auth.service.CustomUserDetailsService;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

  @Lazy
  private final AuthenticationManager authenticationManager;
  @Lazy
  private final CustomUserDetailsService customUserDetailsService;
  private final RedisTemplate<String, String> redisTemplate;
  private final JwtTokenProvider jwtTokenProvider;

  private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
  private static final String BLACKLIST_PREFIX = "blacklist:";
  private static final String REUSED_TOKEN_PREFIX = "reused_token:";
  private static final Duration REFRESH_TOKEN_EXPIRY = Duration.ofDays(7);
  private static final Duration REUSED_TOKEN_EXPIRY = Duration.ofSeconds(10);

  @Transactional
  public LoginResponse login(LoginRequest loginRequest, String clientIP) {
    try {
      Authentication authentication = authenticateUser(loginRequest);
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

      TokenPair tokens = generateTokenPair(authentication);

      storeRefreshToken(userDetails.getUsername(), tokens.getRefreshToken());
      logSecurityEvent("LOGIN_SUCCESS", userDetails.getUsername(), clientIP);

      return LoginResponse.builder()
          .accessToken(tokens.getAccessToken())
          .refreshToken(tokens.getRefreshToken())
          .tokenType("Bearer")
          .success(true)
          .user(UserInfo.from(userDetails))
          .message("로그인 성공")
          .build();
    } catch (BadCredentialsException e) {
      logSecurityEvent("LOGIN_FAILED", loginRequest.getLoginId(), clientIP);
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }
  }

  @Transactional
  public String extractTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7).trim();
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

      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      logSecurityEvent("TOKEN_REFRESH_SUCCESS", loginId, clientIP);

      return TokenRefreshResponse.builder()
          .accessToken(newTokens.getAccessToken())
          .refreshToken(newTokens.getRefreshToken())
          .tokenType("Bearer")
          .success(true)
          .expiresIn(jwtTokenProvider.getExpiration(newTokens.getAccessToken()))
          .user(UserInfo.from(userDetails))
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
  public UserInfo getUserInfo(String accessToken) {
    validateAccessToken(accessToken);

    Claims claims = jwtTokenProvider.getClaims(accessToken);
    String loginId = claims.getSubject();

    CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(
        loginId);

    return UserInfo.builder()
        .loginId(loginId)
        .email(userDetails.getEmail())
        .fullName(userDetails.getFullName())
        .roles(extractRoles(claims))
        .build();
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

  private Authentication authenticateUser(LoginRequest loginRequest) {
    return authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequest.getLoginId(),
            loginRequest.getPassword()
        )
    );
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

    String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + loginId);
    if (storedToken == null || !refreshToken.equals(storedToken)) {
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

  private void storeRefreshToken(String loginId, String refreshToken) {
    String key = REFRESH_TOKEN_PREFIX + loginId;
    redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_EXPIRY);
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
