package com.factseekerbackend.domain.user.entity;

import com.factseekerbackend.domain.analysis.entity.VideoAnalysis;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Entity
@Builder
@Table
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long id;

  @Column(unique = true)
  private String loginId;

  private String password;

  @Column(nullable = false)
  private String fullName;

  private String phone;

  @Column(nullable = false, unique = true)
  private String email;

  @Enumerated(EnumType.STRING)
  @ElementCollection(fetch = FetchType.EAGER)
  private Set<Role> roles;

  @Enumerated(EnumType.STRING)
  private AuthProvider provider;

  private String providerId;

  private Boolean emailVerified = false;

  private boolean isCompleteProfile = false;

  @CreatedDate
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<VideoAnalysis> videoAnalyses;

  public void updatePassword(String newEncodedPassword) {
    if (newEncodedPassword == null || newEncodedPassword.trim().isEmpty()) {
      throw new IllegalArgumentException("새로운 비밀번호는 비어있을 수 없습니다.");
    }
    this.password = newEncodedPassword;
  }

  public boolean isSocialUser() {
    return (provider != null) && (provider != AuthProvider.LOCAL);
  }

  public void updateProfile(String fullName) {
    if (fullName != null) {
      this.fullName = fullName;
    }
  }

}
