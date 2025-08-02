package com.factseekerbackend.domain.user.dto.request;

import lombok.Getter;

@Getter
public class UserRegisterRequest {

  private String loginId;
  private String password;
  private String username;
  private String phone;
  private String email;
}
