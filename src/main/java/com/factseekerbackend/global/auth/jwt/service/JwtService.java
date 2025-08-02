package com.factseekerbackend.global.auth.jwt.service;

import com.factseekerbackend.global.auth.dto.LoginRequest;
import com.factseekerbackend.global.auth.dto.LoginResponse;
import com.factseekerbackend.global.auth.dto.UserInfo;
import com.factseekerbackend.global.auth.jwt.JwtTokenProvider;
import com.factseekerbackend.global.auth.jwt.dto.TokenRefreshResponse;
import com.factseekerbackend.global.exception.InvalidTokenException;
import com.factseekerbackend.global.auth.jwt.CustomUserDetails;
import com.factseekerbackend.global.auth.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
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

  @Transactional
  public LoginResponse login(LoginRequest loginRequest) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getLoginId(),
            loginRequest.getPassword()));

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

    String accessToken = jwtTokenProvider.createAccessToken(authentication);
    String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

    storeRefreshToken(userDetails.getUsername(), refreshToken);
    logLoginActivity(userDetails);

    return LoginResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .success(true)
        .user(UserInfo.from(userDetails))
        .message("로그인 성공")
        .build();
  }

  @Transactional
  public String extractTokenFromRequest(HttpServletRequest request) throws InvalidTokenException {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7).trim();
    }
    throw new InvalidTokenException("제공된 토큰이 없습니다.");
  }

  @Transactional
  public TokenRefreshResponse refreshAccessToken(String refreshToken)
      throws InvalidTokenException {
    if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
      throw new InvalidTokenException("유효하지 않은 리프레시 토큰입니다.");
    }

    String loginId = jwtTokenProvider.getUsernameFromToken(refreshToken);
    String reusedKey = isReused(loginId, refreshToken);
    isValidStoredRefreshToken(loginId, refreshToken);

    CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(
        loginId);
    Authentication newAuth = new UsernamePasswordAuthenticationToken(userDetails, null,
        userDetails.getAuthorities());

    String newAccessToken = jwtTokenProvider.createAccessToken(newAuth);
    String newRefreshToken = jwtTokenProvider.createRefreshToken(newAuth);

    removeRefreshToken(loginId);
    redisTemplate.opsForValue().set(reusedKey, "invalidated", Duration.ofSeconds(10));
    storeRefreshToken(loginId, newRefreshToken);

    return TokenRefreshResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .tokenType("Bearer")
        .success(true)
        .expiresIn(jwtTokenProvider.getExpiration(newAccessToken))
        .user(UserInfo.from(userDetails))
        .message("액세스 토큰 및 리프레시 토큰 갱신 성공")
        .build();
  }

  @Transactional
  public void logout(String accessToken) throws InvalidTokenException {
    if (!jwtTokenProvider.validateAccessToken(accessToken)) {
      throw new InvalidTokenException("유효하지 않은 액세스 토큰입니다.");
    }

    String loginId = jwtTokenProvider.getUsernameFromToken(accessToken);
    CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(
        loginId);

    removeRefreshToken(loginId);

    Long expiration = jwtTokenProvider.getExpiration(accessToken);
    if (expiration > 0) {
      redisTemplate.opsForValue()
          .set("blacklist:" + accessToken, "logout", Duration.ofMillis(expiration));
      log.info("Access Token blacklisted for user {}: {}", loginId, accessToken);
    }

    logLogoutActivity(userDetails);
  }

  @Transactional
  public UserInfo getUserInfo(String accessToken) throws InvalidTokenException {
    if (!jwtTokenProvider.validateAccessToken(accessToken)) {
      throw new InvalidTokenException("유효하지 않은 토큰입니다.");
    }

    Claims claims = jwtTokenProvider.getClaims(accessToken);
    String loginId = jwtTokenProvider.getUsernameFromToken(accessToken);

    return UserInfo.builder()
        .loginId(loginId)
        .roles(extractRoles(claims))
        .build();
  }

  @Transactional
  public boolean isTokenBlacklisted(String accessToken) {
    return redisTemplate.hasKey("blacklist:" + accessToken);
  }

  @Transactional
  public void removeRefreshToken(String loginId) {
    String key = "refresh_token:" + loginId;
    redisTemplate.delete(key);
  }

  @Transactional
  public void revokeAllUserTokens(String loginId) {
    redisTemplate.delete("refresh_token:" + loginId);
    log.info("사용자 {}의 모든 토큰이 무효화되었습니다. 재로그인 필요.", loginId);
  }

  private String isReused(String loginId, String refreshToken) throws InvalidTokenException {
    String reusedKey = "reused_token:" + refreshToken;
    if (redisTemplate.hasKey(reusedKey)) {
      log.warn("Refresh Token 재사용 감지! 사용자: {}", loginId);
      revokeAllUserTokens(loginId);
      throw new InvalidTokenException("리프레시 토큰이 재사용되었습니다. 다시 로그인해주세요.");
    }
    return reusedKey;
  }

  private List<String> extractRoles(Claims claims) {
    @SuppressWarnings("unchecked")
    List<String> roles = (List<String>) claims.get("roles");
    return roles != null ? roles : List.of();
  }

  private void storeRefreshToken(String loginId, String refreshToken) {
    String key = "refresh_token:" + loginId;
    Duration expiration = Duration.ofDays(7);
    redisTemplate.opsForValue().set(key, refreshToken, expiration);
  }

  private void isValidStoredRefreshToken(String loginId, String token)
      throws InvalidTokenException {
    String key = "refresh_token:" + loginId;
    String storedToken = redisTemplate.opsForValue().get(key);

    if (storedToken == null) {
      throw new InvalidTokenException("유효한 리프레시 토큰을 찾을 수 없습니다. 다시 로그인해주세요.");
    }

    if (!token.equals(storedToken)) {
      log.warn("저장된 리프레시 토큰 불일치 감지! 사용자: {}", loginId);
      revokeAllUserTokens(loginId);
      throw new InvalidTokenException("유효하지 않은 리프레시 토큰입니다. 다시 로그인해주세요.");
    }
  }

  private void logLoginActivity(CustomUserDetails userDetails) {
    log.info("User login: {} ({}), Roles: {}",
        userDetails.getUsername(),
        userDetails.getEmail(),
        userDetails.getAuthorities());
  }

  private void logLogoutActivity(CustomUserDetails userDetails) {
    log.info("User logout: {} ({})",
        userDetails.getUsername(),
        userDetails.getEmail());
  }

}
