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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "사용자 관리", description = "사용자 회원가입, 정보 관리, 비밀번호 재설정 API")
@Validated
public class UserController {

  private final UserService userService;

  @Operation(
      summary = "소셜 회원가입 완료",
      description = "소셜 로그인 사용자의 추가 정보 입력을 완료하여 회원가입을 마무리합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "회원가입 완료",
          content = @Content(schema = @Schema(implementation = TokenResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 요청",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      )
  })
  @PostMapping("/social/complete")
  public ResponseEntity<ApiResponse<TokenResponse>> completeSocialSignup(
      @Parameter(description = "소셜 회원가입 완료 요청 정보")
      @Valid @RequestBody CompleteSocialSignupRequest request) {
    try {
      TokenResponse response = userService.completeSocialSignup(request);
      return ResponseEntity.ok(ApiResponse.success("소셜 회원가입이 완료되었습니다.", response));
    } catch (Exception e) {
      log.error("[API] 소셜 회원가입 완료 실패: {}", e.getMessage());
      return ResponseEntity.ok(ApiResponse.error("소셜 회원가입 완료에 실패했습니다: " + e.getMessage()));
    }
  }

  @Operation(
      summary = "아이디 중복 체크",
      description = "회원가입 시 사용할 아이디의 중복 여부를 확인합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "분석 요청 성공",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ApiResponse.class),
              examples = {
                  @ExampleObject(
                      value = "{\n  \"success\": true,\n  \"message\": \"사용 가능한 아이디입니다.\",\n  \"data\": true \n}"
                  )
              }
          )
      )
  })
  @GetMapping("/check/loginId")
  public ResponseEntity<ApiResponse<Boolean>> checkLoginIdAvailability(
      @Parameter(description = "확인할 아이디", example = "user123")
      @RequestParam String loginId) {
    try {
      boolean isAvailable = userService.isLoginIdAvailable(loginId);
      return ResponseEntity.ok(ApiResponse.success(
          isAvailable ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다.",
          isAvailable));
    } catch (Exception e) {
      log.error("[API] 아이디 중복 체크 실패: {}", e.getMessage());
      return ResponseEntity.ok(ApiResponse.error("아이디 중복 체크에 실패했습니다: " + e.getMessage()));
    }
  }

  @Operation(
      summary = "이메일 중복 체크",
      description = "회원가입 시 사용할 이메일의 중복 여부를 확인합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "분석 요청 성공",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ApiResponse.class),
              examples = {
                  @ExampleObject(
                      value = "{\n  \"success\": true,\n  \"message\": \"사용 가능한 이메일입니다.\",\n  \"data\": true \n}"
                  )
              }
          )
      )
  })
  @GetMapping("/check/email")
  public ResponseEntity<ApiResponse<Boolean>> checkEmailAvailability(
      @Parameter(description = "확인할 이메일", example = "user@example.com")
      @RequestParam String email) {
    try {
      boolean isAvailable = userService.isEmailAvailable(email);
      return ResponseEntity.ok(ApiResponse.success(
          isAvailable ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.",
          isAvailable));
    } catch (Exception e) {
      log.error("[API] 이메일 중복 체크 실패: {}", e.getMessage());
      return ResponseEntity.ok(ApiResponse.error("이메일 중복 체크에 실패했습니다: " + e.getMessage()));
    }
  }

  @Operation(
      summary = "인증번호 발송",
      description = "비밀번호 재설정을 위한 인증번호를 이메일로 발송합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "인증번호 발송 성공",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 요청",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      )
  })
  @PostMapping("/auth/send-verification-code")
  public ResponseEntity<ApiResponse<Void>> sendVerificationCode(
      @Parameter(description = "인증번호 발송 요청 정보")
      @Valid @RequestBody ForgotPasswordRequest request) {
    try {
      userService.sendVerificationCode(request);
      return ResponseEntity.ok(ApiResponse.success("인증번호가 이메일로 발송되었습니다. (5분간 유효)"));
    } catch (Exception e) {
      log.error("[API] 인증번호 발송 실패: {}", e.getMessage());
      return ResponseEntity.ok(ApiResponse.error("인증번호 발송에 실패했습니다: " + e.getMessage()));
    }
  }

  @Operation(
      summary = "인증번호 확인",
      description = "발송된 인증번호를 확인하여 임시 토큰을 발급받습니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "인증번호 확인 성공",
          content = @Content(schema = @Schema(implementation = VerifyCodeResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 인증번호",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      )
  })
  @PostMapping("/auth/verify-code")
  public ResponseEntity<ApiResponse<VerifyCodeResponse>> verifyCode(
      @Parameter(description = "인증번호 확인 요청 정보")
      @Valid @RequestBody VerifyCodeRequest request) {
    try {
      String tempToken = userService.verifyCode(request);
      VerifyCodeResponse response = VerifyCodeResponse.builder()
          .tempToken(tempToken)
          .message("인증이 완료되었습니다. 새 비밀번호를 설정해주세요.")
          .build();
      return ResponseEntity.ok(ApiResponse.success("인증 성공", response));
    } catch (Exception e) {
      log.error("[API] 인증번호 확인 실패: {}", e.getMessage());
      return ResponseEntity.ok(ApiResponse.error("인증번호 확인에 실패했습니다: " + e.getMessage()));
    }
  }

  @Operation(
      summary = "비밀번호 재설정",
      description = "임시 토큰을 사용하여 새로운 비밀번호로 재설정합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "비밀번호 재설정 성공",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 요청",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      )
  })
  @PostMapping("/auth/reset-password")
  public ResponseEntity<ApiResponse<Void>> resetPassword(
      @Parameter(description = "비밀번호 재설정 요청 정보")
      @Valid @RequestBody ResetPasswordRequest request) {
    try {
      userService.resetPassword(request);
      return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 재설정되었습니다. 다시 로그인해주세요."));
    } catch (Exception e) {
      log.error("[API] 비밀번호 재설정 실패: {}", e.getMessage());
      return ResponseEntity.ok(ApiResponse.error("비밀번호 재설정에 실패했습니다: " + e.getMessage()));
    }
  }

  @Operation(
      summary = "일반 회원가입",
      description = "아이디, 비밀번호, 이메일 등을 사용하여 새로운 사용자를 등록합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "회원가입 성공",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 요청",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      )
  })
  @PostMapping("/auth/register")
  public ResponseEntity<ApiResponse<Void>> register(
      @Parameter(description = "회원가입 요청 정보")
      @Valid @RequestBody RegisterRequest request) {
    try {
      userService.register(request);
      return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다."));
    } catch (Exception e) {
      log.error("[API] 회원가입 실패: {}", e.getMessage());
      return ResponseEntity.ok(ApiResponse.error("회원가입에 실패했습니다: " + e.getMessage()));
    }
  }

  @Operation(
      summary = "아이디 찾기",
      description = "이메일을 사용하여 등록된 아이디를 찾습니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "아이디 찾기 성공",
          content = @Content(schema = @Schema(implementation = FindIdResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "사용자를 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      )
  })
  @PostMapping("/auth/find-id")
  public ResponseEntity<ApiResponse<FindIdResponse>> findId(
      @Parameter(description = "아이디 찾기 요청 정보")
      @Valid @RequestBody FindIdRequest request) {
    try {
      FindIdResponse response = userService.findLoginId(request);
      return ResponseEntity.ok(ApiResponse.success("아이디 찾기가 완료되었습니다.", response));
    } catch (Exception e) {
      log.error("[API] 아이디 찾기 실패: {}", e.getMessage());
      return ResponseEntity.ok(ApiResponse.error("아이디 찾기에 실패했습니다: " + e.getMessage()));
    }
  }

  @Operation(
      summary = "비밀번호 변경",
      description = "현재 로그인된 사용자의 비밀번호를 변경합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "비밀번호 변경 성공",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 요청",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "401",
          description = "인증되지 않은 요청",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      )
  })
  @PutMapping("/me/password")
  public ResponseEntity<ApiResponse<Void>> changePassword(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Parameter(description = "비밀번호 변경 요청 정보")
      @Valid @RequestBody ChangePasswordRequest request) {
    try {
      userService.changePassword(userDetails.getUsername(), request);
      return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 변경되었습니다."));
    } catch (Exception e) {
      log.error("[API] 비밀번호 변경 실패: {}", e.getMessage());
      return ResponseEntity.ok(ApiResponse.error("비밀번호 변경에 실패했습니다: " + e.getMessage()));
    }
  }

  @Operation(
      summary = "회원탈퇴",
      description = "현재 로그인된 사용자의 계정을 삭제합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "회원탈퇴 성공",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "401",
          description = "인증되지 않은 요청",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      )
  })
  @DeleteMapping("/me")
  public ResponseEntity<ApiResponse<Void>> deleteUser(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    try {
      userService.deleteByLoginId(userDetails.getUsername());
      return ResponseEntity.ok(ApiResponse.success("회원탈퇴가 완료되었습니다."));
    } catch (Exception e) {
      log.error("[API] 회원탈퇴 실패: {}", e.getMessage());
      return ResponseEntity.ok(ApiResponse.error("회원탈퇴에 실패했습니다: " + e.getMessage()));
    }
  }
}
