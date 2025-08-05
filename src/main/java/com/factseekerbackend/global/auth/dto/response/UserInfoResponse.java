package com.factseekerbackend.global.auth.dto.response;

import com.factseekerbackend.domain.user.entity.AuthProvider;
import com.factseekerbackend.domain.user.entity.Role;
import com.factseekerbackend.domain.user.entity.User;
import com.factseekerbackend.global.auth.jwt.CustomUserDetails;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoResponse {

  private Long id;
  private String loginId;
  private String fullname;
  private String phone;
  private String email;
  private Set<Role> roles;
  private AuthProvider provider;
  private boolean isCompleteProfile;
  private LocalDateTime createdAt;

  public static UserInfoResponse from(User user) {
    return UserInfoResponse.builder()
        .id(user.getId())
        .loginId(user.getLoginId())
        .fullname(user.getFullName())
        .phone(user.getPhone())
        .email(user.getEmail())
        .roles(user.getRoles())
        .provider(user.getProvider())
        .isCompleteProfile(user.isCompleteProfile())
        .createdAt(user.getCreatedAt())
        .build();
  }

}
