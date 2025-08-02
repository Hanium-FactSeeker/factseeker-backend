package com.factseekerbackend.domain.user.entity;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
  USER("USER"),
  ADMIN("ADMIN");

  private final String roles;

  public List<String> getRoles() {
    return Arrays.asList(roles.split(","));
  }
}
