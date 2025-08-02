package com.factseekerbackend.domain.user.controller;

import com.factseekerbackend.domain.user.dto.request.ChangePasswordRequest;
import com.factseekerbackend.domain.user.dto.request.UserRegisterRequest;
import com.factseekerbackend.domain.user.service.UserService;
import com.factseekerbackend.global.auth.jwt.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("/auth/register")
  public ResponseEntity<?> register(@RequestBody UserRegisterRequest registerRequest) {
    try {
      userService.register(registerRequest);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PutMapping("/me/password")
  public ResponseEntity<?> changePassword(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody
      ChangePasswordRequest changePasswordRequest){
    if (customUserDetails == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    try {
      userService.changePassword(customUserDetails.getUsername(), changePasswordRequest);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @DeleteMapping("/me")
  public ResponseEntity<?> deleteUser(
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    if (customUserDetails == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    try {
      userService.deleteByLoginId(customUserDetails.getUsername());
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

}
