package com.factseekerbackend.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@Table
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String loginId;

  private String password;

  @Column(nullable = false)
  private String fullName;

  private String phone;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Role role;

  @Enumerated(EnumType.STRING)
  private AuthProvider authProvider;

  private String socialId;

  @Builder.Default
  private Boolean emailVerified = false;

  public void updatePassword(String newEncodedPassword) {
    if (newEncodedPassword == null || newEncodedPassword.trim().isEmpty()) {
      throw new IllegalArgumentException("새로운 비밀번호는 비어있을 수 없습니다.");
    }
    this.password = newEncodedPassword;
  }

  public boolean isSocialUser() {
    return (authProvider != null) && (authProvider != AuthProvider.LOCAL);
  }

  public void updateProfile(String fullName) {
    if (fullName != null) {
      this.fullName = fullName;
    }
  }

}
