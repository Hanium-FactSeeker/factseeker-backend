package com.factseekerbackend.global.auth.jwt.dto;

import com.factseekerbackend.global.auth.dto.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponse {
  private String accessToken;
  private String refreshToken;
  private String tokenType;
  private Long expiresIn;
  private UserInfo user;
  private boolean success = true;
  private String message;

  public static TokenRefreshResponse success(String accessToken, Long expiresIn) {
    return TokenRefreshResponse.builder()
        .accessToken(accessToken)
        .tokenType("Bearer")
        .expiresIn(expiresIn)
        .success(true)
        .build();
  }

  public static TokenRefreshResponse success(String accessToken, Long expiresIn, UserInfo user) {
    return TokenRefreshResponse.builder()
        .accessToken(accessToken)
        .tokenType("Bearer")
        .expiresIn(expiresIn)
        .user(user)
        .success(true)
        .build();
  }

  // 실패 응답 생성
  public static TokenRefreshResponse error(String message) {
    return TokenRefreshResponse.builder()
        .success(false)
        .message(message)
        .build();
  }
}
