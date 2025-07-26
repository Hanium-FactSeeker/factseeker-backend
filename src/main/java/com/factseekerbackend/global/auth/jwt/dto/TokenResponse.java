package com.factseekerbackend.global.auth.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

  private String accessToken;
  private String refreshToken;
  private String tokenType;
  private Long expiresIn;

}
