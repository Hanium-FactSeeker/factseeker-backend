package com.factseekerbackend.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest {

  private String loginId;
  private String password;
  private String username;
  private String phone;
  private String email;
}
