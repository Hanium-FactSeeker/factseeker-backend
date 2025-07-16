package com.factseekerbackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequest {
    private String username;
    private String phone;
    private String email;
    private String password;
}
