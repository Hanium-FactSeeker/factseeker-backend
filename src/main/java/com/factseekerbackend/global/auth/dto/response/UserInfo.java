package com.factseekerbackend.global.auth.dto.response;

import com.factseekerbackend.global.auth.jwt.CustomUserDetails;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

  private String loginId;
  private String email;
  private String fullName;
  private List<String> roles;

  public static UserInfo from(CustomUserDetails userDetails) {
    return UserInfo.builder()
        .loginId(userDetails.getUsername())
        .email(userDetails.getEmail())
        .fullName(userDetails.getFullName())
        .roles(userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()))
        .build();
  }

}
