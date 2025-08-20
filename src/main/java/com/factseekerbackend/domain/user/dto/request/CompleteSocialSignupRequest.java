package com.factseekerbackend.domain.user.dto.request;

import lombok.Data;

@Data
public class CompleteSocialSignupRequest {

  private String tempToken;
  private String fullname;
  private String email;
  private String phone;
}
