package com.factseekerbackend.domain.user.service;

import com.factseekerbackend.domain.user.dto.request.ChangePasswordRequest;
import com.factseekerbackend.domain.user.dto.request.FindIdRequest;
import com.factseekerbackend.domain.user.dto.request.ForgotPasswordRequest;
import com.factseekerbackend.domain.user.dto.request.ResetPasswordRequest;
import com.factseekerbackend.domain.user.dto.request.UserRegisterRequest;
import com.factseekerbackend.domain.user.dto.request.VerifyCodeRequest;
import com.factseekerbackend.domain.user.dto.response.FindIdResponse;
import com.factseekerbackend.domain.user.entity.Role;
import com.factseekerbackend.domain.user.entity.User;
import com.factseekerbackend.domain.user.repository.UserRepository;
import com.factseekerbackend.global.auth.jwt.service.JwtService;
import com.factseekerbackend.global.email.EmailService;
import com.factseekerbackend.global.exception.BusinessException;
import com.factseekerbackend.global.exception.ErrorCode;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final RedisTemplate<String, String> redisTemplate;
  private final EmailService emailService;

  private static final String RATE_LIMIT_PREFIX = "rate_limit:";
  private static final String VERIFY_ATTEMPTS_PREFIX = "verify_attempts:";
  private static final int MAX_SEND_ATTEMPTS = 5; // 15분 내 최대 5회 발송
  private static final int MAX_VERIFY_ATTEMPTS = 10; // 5분 내 최대 10회 검증 시도
  private static final Duration SEND_RATE_LIMIT_DURATION = Duration.ofMinutes(15);
  private static final Duration VERIFY_RATE_LIMIT_DURATION = Duration.ofMinutes(5);

  private static final String VERIFICATION_CODE_PREFIX = "verification_code:";
  private static final String VERIFIED_USER_PREFIX = "verified_user:";
  private static final Duration CODE_EXPIRY = Duration.ofMinutes(5); // 5분
  private static final Duration VERIFIED_EXPIRY = Duration.ofMinutes(10); // 10분

  // 1단계: 인증번호 발송
  @Transactional
  public void sendVerificationCode(ForgotPasswordRequest request) {
    checkSendRateLimit(request.getEmail());

    try {
      User user = userRepository.findByLoginIdAndEmail(request.getLoginId(), request.getEmail())
          .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

      String verificationCode = generateVerificationCode();
      storeVerificationCode(request.getEmail(), verificationCode);
      emailService.sendVerificationCodeEmail(request.getEmail(), verificationCode);

      incrementSendAttempts(request.getEmail());

      log.info("인증 번호 전송: {}", request.getEmail());
    } catch (BusinessException e) {
      // 사용자가 존재하지 않아도 로그만 남기고 정상 응답 반환
      incrementSendAttempts(request.getEmail());
      log.warn("인증 요청한 유저를 찾지 못했습니다.: {}", request.getLoginId());
    }
  }

  // 2단계: 인증번호 확인
  @Transactional
  public String verifyCode(VerifyCodeRequest request) {
    checkVerifyRateLimit(request.getEmail());
    String storedCode = getStoredVerificationCode(request.getEmail());

    if (storedCode == null) {
      incrementVerifyAttempts(request.getEmail());
      throw new BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED);
    }

    if (!storedCode.equals(request.getCode())) {
      incrementVerifyAttempts(request.getEmail());
      throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
    }

    String tempToken = generateTempToken();
    storeVerifiedUser(request.getEmail(), tempToken);

    deleteVerificationCode(request.getEmail());
    clearVerifyAttempts(request.getEmail());

    return tempToken;
  }

  // 3단계: 비밀번호 재설정
  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    String email = getVerifiedUserEmail(request.getTempToken());
    if (email == null) {
      throw new BusinessException(ErrorCode.INVALID_TEMP_TOKEN);
    }

    User user = userRepository.findByLoginIdAndEmail(request.getLoginId(), email)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    validateNewPassword(request.getNewPassword(), user.getPassword());

    String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
    user.updatePassword(encodedNewPassword);

    deleteVerifiedUser(request.getTempToken());
    jwtService.revokeAllUserTokens(user.getLoginId());
  }

  @Transactional
  public void register(UserRegisterRequest request) {
    validateUserRegistration(request);

    User user = User.builder()
        .loginId(request.getLoginId())
        .password(passwordEncoder.encode(request.getPassword()))
        .fullName(request.getFullname())
        .email(request.getEmail())
        .phone(request.getPhone())
        .role(Role.USER)
        .build();

    emailService.sendWelcomeEmail(request.getEmail(),request.getFullname());

    userRepository.save(user);

  }

  @Transactional
  public void changePassword(String loginId, ChangePasswordRequest changePasswordRequest) {
    User user = findUserByLoginId(loginId);
    validatePasswordChange(user, changePasswordRequest);

    String encodedNewPassword = passwordEncoder.encode(changePasswordRequest.getNewPassword());
    user.updatePassword(encodedNewPassword);

    jwtService.revokeAllUserTokens(loginId);
  }

  @Transactional
  public void deleteByLoginId(String loginId) {
    userRepository.deleteByLoginId(loginId);
    jwtService.removeRefreshToken(loginId);
  }

  @Transactional(readOnly = true)
  public FindIdResponse findLoginId(FindIdRequest request) {
    if (!StringUtils.hasText(request.getEmail())) {
      return FindIdResponse.builder()
          .success(false)
          .message("이메일을 입력해주세요.")
          .build();
    }

    return userRepository.findByEmail(request.getEmail())
        .map(user -> FindIdResponse.builder()
            .success(true)
            .loginId(maskLoginId(user.getLoginId())) // 보안을 위한 마스킹
            .message("아이디를 찾았습니다.")
            .build())
        .orElse(FindIdResponse.builder()
            .success(false)
            .message("입력하신 정보와 일치하는 아이디를 찾을 수 없습니다.")
            .build());
  }

  private String generateVerificationCode() {
    Random random = new Random();
    return String.format("%08d", random.nextInt(100000000));
  }

  private String generateTempToken() {
    return UUID.randomUUID().toString();
  }

  private void storeVerificationCode(String email, String code) {
    redisTemplate.opsForValue().set(VERIFICATION_CODE_PREFIX + email, code, CODE_EXPIRY);
  }

  private String getStoredVerificationCode(String email) {
    return redisTemplate.opsForValue().get(VERIFICATION_CODE_PREFIX + email);
  }

  private void deleteVerificationCode(String email) {
    redisTemplate.delete(VERIFICATION_CODE_PREFIX + email);
  }

  private void storeVerifiedUser(String email, String tempToken) {
    redisTemplate.opsForValue().set(VERIFIED_USER_PREFIX + tempToken, email, VERIFIED_EXPIRY);
  }

  private String getVerifiedUserEmail(String tempToken) {
    return redisTemplate.opsForValue().get(VERIFIED_USER_PREFIX + tempToken);
  }

  private void deleteVerifiedUser(String tempToken) {
    redisTemplate.delete(VERIFIED_USER_PREFIX + tempToken);
  }

  private User findUserByLoginId(String loginId) {
    return userRepository.findByLoginId(loginId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }

  private void validateNewPassword(String newPassword, String currentEncodedPassword) {
    if (passwordEncoder.matches(newPassword, currentEncodedPassword)) {
      throw new BusinessException(ErrorCode.SAME_AS_CURRENT_PASSWORD);
    }
  }

  private void validatePasswordChange(User user, ChangePasswordRequest changePasswordRequest) {
    if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
      throw new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD);
    }

    validateNewPassword(changePasswordRequest.getNewPassword(), user.getPassword());
  }

  private void validateUserRegistration(UserRegisterRequest request) {
    if (userRepository.existsByLoginId(request.getLoginId())) {
      throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
    }
  }

  private String maskLoginId(String loginId) {
    if (loginId.length() <= 3) {
      return loginId.charAt(0) + "*".repeat(loginId.length() - 1);
    }
    int visibleChars = Math.max(2, (loginId.length() * 2) / 3);
    return loginId.substring(0, visibleChars) +
        "*".repeat(loginId.length() - visibleChars);
  }

  private void checkSendRateLimit(String email) {
    String key = RATE_LIMIT_PREFIX + "send:" + email;
    String attempts = redisTemplate.opsForValue().get(key);

    if (attempts != null && Integer.parseInt(attempts) >= MAX_SEND_ATTEMPTS) {
      log.warn("인증번호 발송 횟수 초과: {}", email);
      throw new BusinessException(ErrorCode.TOO_MANY_SEND_ATTEMPTS);
    }
  }

  private void checkVerifyRateLimit(String email) {
    String key = VERIFY_ATTEMPTS_PREFIX + email;
    String attempts = redisTemplate.opsForValue().get(key);

    if (attempts != null && Integer.parseInt(attempts) >= MAX_VERIFY_ATTEMPTS) {
      log.warn("인증번호 검증 시도 횟수 초과: {}", email);
      throw new BusinessException(ErrorCode.TOO_MANY_VERIFY_ATTEMPTS);
    }
  }

  private void incrementSendAttempts(String email) {
    String key = RATE_LIMIT_PREFIX + "send:" + email;
    String currentAttempts = redisTemplate.opsForValue().get(key);

    if (currentAttempts == null) {
      // 첫 번째 시도
      redisTemplate.opsForValue().set(key, "1", SEND_RATE_LIMIT_DURATION);
    } else {
      // 기존 시도 횟수 증가
      redisTemplate.opsForValue().increment(key);
    }
  }

  private void incrementVerifyAttempts(String email) {
    String key = VERIFY_ATTEMPTS_PREFIX + email;
    String currentAttempts = redisTemplate.opsForValue().get(key);

    if (currentAttempts == null) {
      // 첫 번째 시도 - 인증번호 유효시간과 동일하게 설정
      redisTemplate.opsForValue().set(key, "1", VERIFY_RATE_LIMIT_DURATION);
    } else {
      // 기존 시도 횟수 증가
      redisTemplate.opsForValue().increment(key);
    }
  }

  private void clearVerifyAttempts(String email) {
    String key = VERIFY_ATTEMPTS_PREFIX + email;
    redisTemplate.delete(key);
  }

  // 관리자용: 특정 이메일의 Rate Limit 초기화
  public void clearRateLimit(String email) {
    redisTemplate.delete(RATE_LIMIT_PREFIX + "send:" + email);
    redisTemplate.delete(VERIFY_ATTEMPTS_PREFIX + email);
    log.info("Rate limit 초기화: {}", email);
  }

}