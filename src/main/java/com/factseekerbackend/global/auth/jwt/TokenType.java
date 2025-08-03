package com.factseekerbackend.global.auth.jwt;

public enum TokenType {
  ACCESS("access"),
  REFRESH("refresh");

  private final String value;

  TokenType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
