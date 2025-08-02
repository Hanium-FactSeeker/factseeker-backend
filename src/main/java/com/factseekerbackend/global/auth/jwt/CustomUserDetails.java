package com.factseekerbackend.global.auth.jwt;

import com.factseekerbackend.domain.user.entity.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

  private final User user;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<GrantedAuthority> authorities = new ArrayList<>();
    user.getRole().getRoles()
        .forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
    return authorities;
  }

  public Long getId() {
    return user.getId();
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() { // 이곳의 username은 로그인 시 사용하는 아이디를 의미
    return user.getLoginId();
  }

  public String getEmail() {
    return user.getEmail();
  }

  public String getFullName() {
    return user.getFullName();
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

