package com.factseekerbackend.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

  private String tempToken;
  private String loginId;
  private String newPassword;


}
