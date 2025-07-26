package com.factseekerbackend.global.auth.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshRequest {

  private String refreshToken;
}
