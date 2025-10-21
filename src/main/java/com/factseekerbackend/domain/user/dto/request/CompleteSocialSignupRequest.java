package com.factseekerbackend.domain.user.dto.request;

import lombok.Data;

@Data
public class CompleteSocialSignupRequest {

  private String tempToken;
  private String fullname;
  private String gender;
  private String ageRange;
  private String phone;
}
