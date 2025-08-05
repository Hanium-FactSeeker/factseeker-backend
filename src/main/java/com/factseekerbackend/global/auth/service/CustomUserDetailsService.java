package com.factseekerbackend.global.auth.service;

import com.factseekerbackend.domain.user.entity.User;
import com.factseekerbackend.domain.user.repository.UserRepository;
import com.factseekerbackend.global.auth.jwt.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
    User user = userRepository.findByLoginId(loginId)
        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId));

    return CustomUserDetails.create(user);
  }

}
