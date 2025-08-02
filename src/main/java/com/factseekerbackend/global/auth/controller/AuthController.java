package com.factseekerbackend.global.auth.controller;

import com.factseekerbackend.global.auth.dto.request.LoginRequest;
import com.factseekerbackend.global.auth.dto.request.LoginResponse;
import com.factseekerbackend.global.auth.dto.response.UserInfo;
import com.factseekerbackend.global.auth.jwt.dto.TokenRefreshRequest;
import com.factseekerbackend.global.auth.jwt.dto.TokenRefreshResponse;
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
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final JwtService jwtService;

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest,
      HttpServletRequest request) {
    String clientIP = extractClientIP(request);

    LoginResponse response = jwtService.login(loginRequest, clientIP);
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
  public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser(HttpServletRequest request) {
    String token = jwtService.extractTokenFromRequest(request);
    UserInfo userInfo = jwtService.getUserInfo(token);

    return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회 성공", userInfo));
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
