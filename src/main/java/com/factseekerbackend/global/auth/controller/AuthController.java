package com.factseekerbackend.global.auth.controller;

import com.factseekerbackend.global.auth.dto.LoginRequest;
import com.factseekerbackend.global.auth.dto.LoginResponse;
import com.factseekerbackend.global.auth.jwt.dto.TokenRefreshRequest;
import com.factseekerbackend.global.auth.jwt.dto.TokenRefreshResponse;
import com.factseekerbackend.global.auth.jwt.service.JwtService;
import com.factseekerbackend.global.exception.InvalidTokenException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
    try {
      LoginResponse response = jwtService.login(loginRequest);
      return ResponseEntity.ok(response);
    } catch (BadCredentialsException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(LoginResponse.error("잘못된 인증 정보입니다."));
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenRefreshResponse> refreshToken(
      @RequestBody TokenRefreshRequest refreshTokenRequest) {
    try {
      TokenRefreshResponse response = jwtService.refreshAccessToken(
          refreshTokenRequest.getRefreshToken());
      return ResponseEntity.ok(response);
    } catch (InvalidTokenException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(TokenRefreshResponse.error("유효하지 않은 리프레시 토큰입니다."));
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletRequest request) {
    try {
      String token = jwtService.extractTokenFromRequest(request);
      jwtService.logout(token);
      return ResponseEntity.ok().build();
    } catch (InvalidTokenException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(TokenRefreshResponse.error("유효하지 않은 리프레시 토큰입니다."));
    }
  }

}
