package com.factseekerbackend.global.auth.oauth2.dto;

import com.factseekerbackend.domain.user.entity.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserInfo {

  private String providerId;
  private AuthProvider provider;
  private String email;
  private String name;
  private String tempToken;
}
