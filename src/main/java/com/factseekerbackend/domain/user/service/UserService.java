package com.factseekerbackend.domain.user.service;

import com.factseekerbackend.domain.user.dto.request.ChangePasswordRequest;
import com.factseekerbackend.domain.user.dto.request.UserRegisterRequest;
import com.factseekerbackend.domain.user.entity.Role;
import com.factseekerbackend.domain.user.entity.User;
import com.factseekerbackend.domain.user.repository.UserRepository;
import com.factseekerbackend.global.auth.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  @Transactional
  public void register(UserRegisterRequest request) {
    validateUser(request);

    User user = User.builder()
        .loginId(request.getLoginId())
        .password(passwordEncoder.encode(request.getPassword()))
        .fullName(request.getUsername())
        .email(request.getEmail())
        .phone(request.getPhone())
        .role(Role.USER)
        .build();

    userRepository.save(user);
  }

  @Transactional
  public void changePassword(String loginId, ChangePasswordRequest changePasswordRequest) {
    User user = userRepository.findByLoginId(loginId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    validatePasswordChange(user, changePasswordRequest);
    String encodedNewPassword = passwordEncoder.encode(changePasswordRequest.getNewPassword());

    user.updatePassword(encodedNewPassword);
//    userRepository.save(user);

    jwtService.revokeAllUserTokens(loginId);
  }

  @Transactional
  public void deleteByLoginId(String loginId) {
    userRepository.deleteByLoginId(loginId);
    jwtService.removeRefreshToken(loginId);
  }

  private void validatePasswordChange(User user, ChangePasswordRequest changePasswordRequest) {
    if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
      throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
    }

    if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), user.getPassword())) {
      throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
    }
  }

  private void validateUser(UserRegisterRequest request) {
    if (userRepository.existsByLoginId(request.getLoginId())) {
      throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("이미 등록된 이메일입니다.");
    }
  }

}