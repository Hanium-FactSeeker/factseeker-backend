package com.factseekerbackend.global.auth.jwt;

import com.factseekerbackend.domain.user.entity.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

  private Long id;
  private String loginId;
  private String email;
  private String password;
  private String fullName;
  private Collection<? extends GrantedAuthority> authorities;

  public static CustomUserDetails create(User user) {
    Collection<GrantedAuthority> authorities = user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
        .collect(Collectors.toList());

    return new CustomUserDetails(
        user.getId(),
        user.getLoginId(),
        user.getEmail(),
        user.getPassword(),
        user.getFullName(),
        authorities
    );
  }

  @Override
  public String getUsername() {
    return loginId;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

}

