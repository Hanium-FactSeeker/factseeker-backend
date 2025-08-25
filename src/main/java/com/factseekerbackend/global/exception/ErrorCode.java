package com.factseekerbackend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // User 관련 에러
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다."),
  DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 등록된 이메일입니다."),
  INVALID_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다."),
  SAME_AS_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "새 비밀번호는 현재 비밀번호와 달라야 합니다."),

  //비밀번호 찾기 관련
  INVALID_RESET_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않거나 만료된 비밀번호 재설정 토큰입니다."),
  VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "인증번호가 만료되었습니다."),
  INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "잘못된 인증번호입니다."),
  INVALID_TEMP_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 임시 토큰입니다."),
  TOO_MANY_SEND_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS,
      "인증번호 발송 횟수를 초과했습니다. 15분 후 다시 시도해주세요."),
  TOO_MANY_VERIFY_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS,
      "인증번호 검증 시도 횟수를 초과했습니다. 5분 후 다시 시도해주세요."),

  // JWT 토큰 관련
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "잘못된 인증 정보입니다"),
  TOKEN_REUSE_DETECTED(HttpStatus.FORBIDDEN, "토큰 재사용이 감지되었습니다"),
  SUSPICIOUS_ACTIVITY(HttpStatus.TOO_MANY_REQUESTS, "의심스러운 활동이 감지되었습니다"),
  TOKEN_REFRESH_FAILED(HttpStatus.BAD_REQUEST, "토큰 갱신에 실패했습니다"),
  TOKEN_PARSING_FAILED(HttpStatus.BAD_REQUEST, "토큰 파싱에 실패했습니다"),

  // OAuth 관련
  OAUTH2_AUTHENTICATION_PROCESSING_ERROR(HttpStatus.BAD_REQUEST, "OAuth2 인증 처리 중 오류가 발생했습니다."),
  OAUTH2_EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "소셜 로그인에서 이메일 정보를 찾을 수 없습니다."),
  OAUTH2_PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth2 제공업체입니다."),
  OAUTH2_UNAUTHORIZED_REDIRECT_URI(HttpStatus.BAD_REQUEST, "승인되지 않은 리다이렉트 URI입니다."),

  // Trend 관련 에러
  TRENDS_NOT_AVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "현재 트렌드 데이터를 조회할 수 없습니다. 잠시 후 다시 시도해주세요."),

  // 공통 에러
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

  // TOP10 에러
  VIDEO_NOT_FOUND(HttpStatus.BAD_REQUEST, "유효하지 않은 비디오ID 입니다.");
  private final HttpStatus status;
  private final String message;
}
