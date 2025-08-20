package com.factseekerbackend.global.auth.oauth2;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
@Builder
public class CustomOAuth2User implements OAuth2User {

  private final Map<String, Object> attributes;
  private final String nameAttributeKey;
  private final Set<GrantedAuthority> authorities;
  private final String loginId;
  private final boolean isNewUser;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public String getName() {
    return this.loginId;
  }

}
