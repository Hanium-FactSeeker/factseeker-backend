package com.factseekerbackend.global.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

  private String accessToken;
  private String refreshToken;
  private String tokenType = "Bearer";
  private UserInfo user;
  private boolean success = true;
  private String message;

  public static LoginResponse error(String message) {
    return LoginResponse.builder()
        .success(false)
        .message(message)
        .build();
  }
}
