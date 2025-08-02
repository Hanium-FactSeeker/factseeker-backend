package com.factseekerbackend.domain.user.service;

import com.factseekerbackend.domain.user.dto.UserRegisterRequest;
import com.factseekerbackend.domain.user.entity.Role;
import com.factseekerbackend.domain.user.entity.User;
import com.factseekerbackend.domain.user.repository.UserRepository;
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

  private void validateUser(UserRegisterRequest request) {
    if (userRepository.existsByLoginId(request.getLoginId())) {
      throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("이미 등록된 이메일입니다.");
    }
  }
}