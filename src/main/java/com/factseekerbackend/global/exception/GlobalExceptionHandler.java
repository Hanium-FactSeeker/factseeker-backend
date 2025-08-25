package com.factseekerbackend.global.exception;

import com.factseekerbackend.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
    log.warn("BusinessException: {}", e.getMessage());
    return ResponseEntity
        .status(e.getErrorCode().getStatus())
        .body(ApiResponse.error(e.getErrorCode().getMessage()));
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
  public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception e) {
    String message = "입력값을 확인해주세요.";

    if (e instanceof MethodArgumentNotValidException) {
      MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
      if (ex.getBindingResult().hasFieldErrors()) {
        message = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
      }
    } else if (e instanceof BindException) {
      BindException ex = (BindException) e;
      if (ex.getBindingResult().hasFieldErrors()) {
        message = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
      }
    }

    log.warn("인증 에러: {}", message);
    return ResponseEntity.badRequest().body(ApiResponse.error(message));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse> handleBadCredentialsException(BadCredentialsException ex) {
    log.warn("인증 실패: {}", ex.getMessage());
    return ResponseEntity.status(ErrorCode.INVALID_CREDENTIALS.getStatus())
        .body(ApiResponse.error(ex.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
    log.warn("잘못된 요청: {}", ex.getMessage());
      return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
        .body(ApiResponse.error(ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    log.error("예상치 못한 에러: ", e);
    return ResponseEntity
        .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
        .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
  }

}
