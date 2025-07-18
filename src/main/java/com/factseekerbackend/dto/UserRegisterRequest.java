package com.factseekerbackend.dto;

import com.factseekerbackend.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Setter
public class UserRegisterRequest {
    private String username;
    private String phone;
    private String email;
    private String password;

    public User toEntity(PasswordEncoder passwordEncoder) {
        return new User(username, phone, email, passwordEncoder.encode(password));
    }
}
