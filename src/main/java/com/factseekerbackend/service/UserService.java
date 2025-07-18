package com.factseekerbackend.service;

import com.factseekerbackend.dto.UserRegisterRequest;
import com.factseekerbackend.entity.User;
import com.factseekerbackend.repository.UserRepository;
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

    /**
     * 검증을 private 메서드로 뺐습니다. <- 확인 후 삭제
     * @param request
     */

    public void register(UserRegisterRequest request) {
        validateUser(request);

        User user = request.toEntity(passwordEncoder);
        userRepository.save(user);
    }

    private void validateUser(UserRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }
    }
}