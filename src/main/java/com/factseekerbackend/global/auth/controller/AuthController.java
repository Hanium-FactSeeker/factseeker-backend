package com.factseekerbackend.global.auth.controller;

import com.factseekerbackend.global.auth.service.AuthService;
import com.factseekerbackend.global.auth.dto.request.LoginRequest;
import com.factseekerbackend.global.auth.dto.request.LoginResponse;
import com.factseekerbackend.global.auth.dto.request.SocialUserInfoResponse;
import com.factseekerbackend.global.auth.dto.response.UserInfoResponse;
import com.factseekerbackend.global.auth.jwt.dto.request.TokenRefreshRequest;
import com.factseekerbackend.global.auth.jwt.dto.response.TokenRefreshResponse;
import com.factseekerbackend.global.auth.jwt.service.JwtService;
import com.factseekerbackend.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final JwtService jwtService;
  private final AuthService authService;

  // 소셜 로그인 후 추가 정보 입력을 위한 임시 토큰 검증
  @GetMapping("/social/verify")
  public ResponseEntity<SocialUserInfoResponse> verifySocialToken(@RequestParam String token) {
    SocialUserInfoResponse response = jwtService.verifySocialToken(token);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest,
      HttpServletRequest request) {
    String clientIP = extractClientIP(request);

    LoginResponse response = authService.login(loginRequest, clientIP);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenRefreshResponse> refreshToken(
      @RequestBody TokenRefreshRequest refreshTokenRequest, HttpServletRequest httpRequest) {
    String clientIP = extractClientIP(httpRequest);

    TokenRefreshResponse response = jwtService.refreshAccessToken(
        refreshTokenRequest.getRefreshToken(), clientIP);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletRequest request) {
    String clientIP = extractClientIP(request);

    String token = jwtService.extractTokenFromRequest(request);
    jwtService.logout(token, clientIP);
    return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다."));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser(HttpServletRequest request) {
    String token = jwtService.extractTokenFromRequest(request);
    UserInfoResponse userInfoResponse = jwtService.getUserInfo(token);

    return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회 성공", userInfoResponse));
  }

  private String extractClientIP(HttpServletRequest request) {
    String xfHeader = request.getHeader("X-Forwarded-For");
    if (xfHeader != null && !xfHeader.isEmpty()) {
      return xfHeader.split(",")[0].trim();
    }

    String xrHeader = request.getHeader("X-Real-IP");
    if (xrHeader != null && !xrHeader.isEmpty()) {
      return xrHeader;
    }

    return request.getRemoteAddr();
  }

}
