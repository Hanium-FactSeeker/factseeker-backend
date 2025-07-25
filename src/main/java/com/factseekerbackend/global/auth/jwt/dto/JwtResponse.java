package com.factseekerbackend.global.auth.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

  private String accessToken;
  private String refreshToken;

}
