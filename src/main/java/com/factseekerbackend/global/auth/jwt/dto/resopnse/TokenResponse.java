package com.factseekerbackend.global.auth.jwt.dto.resopnse;

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
  private String tokenType = "Bearer";
  private long expiresIn;

}
