package com.factseekerbackend.global.auth.controller;

import com.factseekerbackend.global.auth.dto.response.UserInfo;
import com.factseekerbackend.global.auth.jwt.CustomUserDetails;
import com.factseekerbackend.global.auth.jwt.JwtTokenProvider;
import com.factseekerbackend.global.auth.jwt.service.JwtService;
import com.factseekerbackend.global.auth.oauth2.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class OAuth2TestController {

  @Lazy
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtService jwtService;
  private final RedisTemplate<String, String> redisTemplate;

  private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
  private static final Duration REFRESH_TOKEN_EXPIRY = Duration.ofDays(7);

  /**
   * 공개 엔드포인트 - 인증 없이 접근 가능
   */
  @GetMapping("/public")
  public ResponseEntity<Map<String, String>> publicEndpoint() {
    return ResponseEntity.ok(Map.of(
        "message", "Public endpoint - 인증 없이 접근 가능합니다.",
        "timestamp", String.valueOf(System.currentTimeMillis())
    ));
  }

  /**
   * 보호된 엔드포인트 - JWT 토큰 필요
   */
  @GetMapping("/protected")
  public ResponseEntity<Map<String, Object>> protectedEndpoint(Authentication authentication,
      HttpServletRequest request) {
    log.info("보호된 엔드포인트 접근 - 사용자: {}", authentication.getName());

    String clientIP = getClientIP(request);

    // JWT 토큰에서 사용자 정보 추출
    UserInfo userInfo = null;
    if (authentication.getPrincipal() instanceof CustomUserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      userInfo = UserInfo.from(userDetails);
    }

    return ResponseEntity.ok(Map.of(
        "success", true,
        "message", "보호된 엔드포인트에 성공적으로 접근했습니다.",
        "user", authentication.getName(),
        "authorities", authentication.getAuthorities(),
        "userInfo", userInfo != null ? userInfo : "N/A",
        "clientIP", clientIP,
        "timestamp", System.currentTimeMillis()
    ));
  }

  /**
   * OAuth2 로그인 상태에서 JWT 토큰 발급 (세션 기반 OAuth2용)
   * 일반적으로는 OAuth2AuthenticationSuccessHandler에서 처리되지만,
   * 테스트 목적으로 수동 토큰 발급이 필요한 경우 사용
   */
  @PostMapping("/oauth2/token")
  public ResponseEntity<Map<String, Object>> generateTokenForOAuth2User(
      @AuthenticationPrincipal OAuth2User oauth2User,
      HttpServletRequest request) {

    if (oauth2User == null) {
      return ResponseEntity.badRequest()
          .body(Map.of(
              "success", false,
              "error", "OAuth2 인증이 필요합니다.",
              "message", "먼저 /oauth2/authorize/naver 또는 /oauth2/authorize/kakao로 로그인하세요."
          ));
    }

    try {
      // CustomOAuth2User로 캐스팅
      CustomOAuth2User customOAuth2User = (CustomOAuth2User) oauth2User;

      // JWT 토큰 생성 - JwtService와 동일한 방식
      String accessToken = jwtTokenProvider.createAccessToken(customOAuth2User.getEmail());
      String refreshToken = jwtTokenProvider.createRefreshToken(customOAuth2User.getEmail());

      // Refresh Token Redis에 저장
      storeRefreshToken(customOAuth2User.getEmail(), refreshToken);

      // 사용자 정보 생성
      UserInfo userInfo = UserInfo.builder()
          .loginId(customOAuth2User.getEmail())
          .email(customOAuth2User.getEmail())
          .fullName(customOAuth2User.getFullName())
          .roles(customOAuth2User.getAuthorities().stream()
              .map(authority -> authority.getAuthority())
              .toList())
          .build();

      String clientIP = getClientIP(request);
      log.info("SECURITY_EVENT: MANUAL_TOKEN_GENERATION - User: {}, IP: {}",
          customOAuth2User.getEmail(), clientIP);

      return ResponseEntity.ok(Map.of(
          "success", true,
          "message", "JWT 토큰이 성공적으로 생성되었습니다.",
          "access_token", accessToken,
          "refresh_token", refreshToken,
          "token_type", "Bearer",
          "expires_in", jwtTokenProvider.getExpiration(accessToken) / 1000, // 초 단위
          "user", userInfo
      ));

    } catch (Exception e) {
      log.error("OAuth2 사용자 토큰 생성 중 오류 발생", e);
      return ResponseEntity.internalServerError()
          .body(Map.of(
              "success", false,
              "error", "토큰 생성 중 오류가 발생했습니다.",
              "message", e.getMessage()
          ));
    }
  }

  /**
   * 현재 인증된 사용자 정보 조회 (JWT 또는 OAuth2 세션)
   */
  @GetMapping("/me")
  public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication,
      HttpServletRequest request) {

    String authType = "Unknown";
    Object userDetails = null;

    if (authentication.getPrincipal() instanceof CustomUserDetails) {
      authType = "JWT";
      userDetails = UserInfo.from((CustomUserDetails) authentication.getPrincipal());
    } else if (authentication.getPrincipal() instanceof CustomOAuth2User) {
      authType = "OAuth2 Session";
      CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
      userDetails = Map.of(
          "email", oauth2User.getEmail(),
          "fullName", oauth2User.getFullName(),
          "role", oauth2User.getRole().toString()
      );
    }

    return ResponseEntity.ok(Map.of(
        "success", true,
        "authenticated", authentication.isAuthenticated(),
        "authType", authType,
        "name", authentication.getName(),
        "authorities", authentication.getAuthorities(),
        "userDetails", userDetails,
        "clientIP", getClientIP(request)
    ));
  }

  /**
   * JWT 토큰 검증 테스트
   */
  @PostMapping("/validate-token")
  public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
    String token = request.get("token");

    if (token == null || token.trim().isEmpty()) {
      return ResponseEntity.badRequest()
          .body(Map.of(
              "success", false,
              "valid", false,
              "error", "토큰이 제공되지 않았습니다."
          ));
    }

    try {
      boolean isValid = jwtTokenProvider.validateAccessToken(token);
      boolean isBlacklisted = jwtService.isTokenBlacklisted(token);

      if (isValid && !isBlacklisted) {
        String username = jwtTokenProvider.getUsernameFromToken(token);
        long expiration = jwtTokenProvider.getExpiration(token);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "valid", true,
            "username", username,
            "expiresIn", expiration / 1000,
            "blacklisted", false
        ));
      } else {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "valid", false,
            "blacklisted", isBlacklisted,
            "reason", isBlacklisted ? "Token is blacklisted" : "Token is invalid or expired"
        ));
      }

    } catch (Exception e) {
      return ResponseEntity.ok(Map.of(
          "success", true,
          "valid", false,
          "error", e.getMessage()
      ));
    }
  }

  /**
   * Refresh Token을 Redis에 저장
   */
  private void storeRefreshToken(String email, String refreshToken) {
    String key = REFRESH_TOKEN_PREFIX + email;
    redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_EXPIRY);
    log.debug("Refresh token stored for user: {}", email);
  }

  /**
   * 클라이언트 IP 추출
   */
  private String getClientIP(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }

    String xRealIP = request.getHeader("X-Real-IP");
    if (xRealIP != null && !xRealIP.isEmpty()) {
      return xRealIP;
    }

    return request.getRemoteAddr();
  }
}