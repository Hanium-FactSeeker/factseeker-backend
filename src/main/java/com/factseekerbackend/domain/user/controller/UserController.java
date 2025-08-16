package com.factseekerbackend.domain.user.controller;

import com.factseekerbackend.domain.user.dto.request.ChangePasswordRequest;
import com.factseekerbackend.domain.user.dto.request.CompleteSocialSignupRequest;
import com.factseekerbackend.domain.user.dto.request.FindIdRequest;
import com.factseekerbackend.domain.user.dto.request.ForgotPasswordRequest;
import com.factseekerbackend.domain.user.dto.request.ResetPasswordRequest;
import com.factseekerbackend.domain.user.dto.request.RegisterRequest;
import com.factseekerbackend.domain.user.dto.request.VerifyCodeRequest;
import com.factseekerbackend.domain.user.dto.response.FindIdResponse;
import com.factseekerbackend.domain.user.dto.response.VerifyCodeResponse;
import com.factseekerbackend.domain.user.service.UserService;
import com.factseekerbackend.domain.user.entity.CustomUserDetails;
import com.factseekerbackend.global.auth.jwt.dto.response.TokenResponse;
import com.factseekerbackend.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  // 소셜 로그인 사용자 회원가입 완료
  @PostMapping("/social/complete")
  public ResponseEntity<TokenResponse> completeSocialSignup(
      @RequestBody CompleteSocialSignupRequest request) {
    TokenResponse response = userService.completeSocialSignup(request);
    return ResponseEntity.ok(response);
  }

  // 아이디 중복 체크
  @GetMapping("/check/loginId")
  public ResponseEntity<ApiResponse> checkLoginIdAvailability(@RequestParam String loginId) {
    boolean isAvailable = userService.isLoginIdAvailable(loginId);
    return ResponseEntity.ok(
        ApiResponse.success(isAvailable ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다.", isAvailable));
  }

  // 이메일 중복 체크
  @GetMapping("/check/email")
  public ResponseEntity<ApiResponse> checkEmailAvailability(@RequestParam String email) {
    boolean isAvailable = userService.isEmailAvailable(email);
    return ResponseEntity.ok(
        ApiResponse.success(isAvailable ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.", isAvailable));
  }

  // === 인증번호 기반 비밀번호 재설정 (3단계) ===
  // 1단계: 인증번호 발송
  @PostMapping("/auth/send-verification-code")
  public ResponseEntity<ApiResponse<Void>> sendVerificationCode(
      @RequestBody ForgotPasswordRequest request) {
    userService.sendVerificationCode(request);
    return ResponseEntity.ok(ApiResponse.success("인증번호가 이메일로 발송되었습니다. (5분간 유효)"));
  }

  // 2단계: 인증번호 확인
  @PostMapping("/auth/verify-code")
  public ResponseEntity<ApiResponse<VerifyCodeResponse>> verifyCode(
      @RequestBody VerifyCodeRequest request) {
    String tempToken = userService.verifyCode(request);

    VerifyCodeResponse response = VerifyCodeResponse.builder()
        .tempToken(tempToken)
        .message("인증이 완료되었습니다. 새 비밀번호를 설정해주세요.")
        .build();

    return ResponseEntity.ok(ApiResponse.success("인증 성공", response));
  }

  // 3단계: 비밀번호 재설정
  @PostMapping("/auth/reset-password")
  public ResponseEntity<ApiResponse<Void>> resetPassword(
      @RequestBody ResetPasswordRequest request) {
    userService.resetPassword(request);
    return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 재설정되었습니다. 다시 로그인해주세요."));
  }

  @PostMapping("/auth/register")
  public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequest request) {
    userService.register(request);
    return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다."));
  }

  @PostMapping("/auth/find-id")
  public ResponseEntity<FindIdResponse> findId(@RequestBody FindIdRequest request) {
    FindIdResponse response = userService.findLoginId(request);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/me/password")
  public ResponseEntity<ApiResponse<Void>> changePassword(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody ChangePasswordRequest request) {

    userService.changePassword(userDetails.getUsername(), request);
    return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 변경되었습니다."));
  }

  @DeleteMapping("/me")
  public ResponseEntity<ApiResponse<Void>> deleteUser(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    userService.deleteByLoginId(userDetails.getUsername());
    return ResponseEntity.ok(ApiResponse.success("회원탈퇴가 완료되었습니다."));
  }

}
