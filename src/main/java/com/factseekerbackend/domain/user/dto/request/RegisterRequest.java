package com.factseekerbackend.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

  private String loginId;
  private String password;
  private String fullname;
  private String gender;
  private String ageRange;
  private String phone;
  private String email;
}
