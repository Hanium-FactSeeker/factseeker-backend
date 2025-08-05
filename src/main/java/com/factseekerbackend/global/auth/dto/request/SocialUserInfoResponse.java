package com.factseekerbackend.global.auth.dto.request;

import com.factseekerbackend.domain.user.entity.AuthProvider;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SocialUserInfoResponse {

  private String email;
  private String name;
  private AuthProvider provider;
}
