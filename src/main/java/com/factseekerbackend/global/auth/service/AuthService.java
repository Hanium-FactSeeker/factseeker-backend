package com.factseekerbackend.global.auth.service;

import com.factseekerbackend.domain.user.entity.User;
import com.factseekerbackend.domain.user.repository.UserRepository;
import com.factseekerbackend.global.auth.dto.request.LoginRequest;
import com.factseekerbackend.global.auth.dto.request.LoginResponse;
import com.factseekerbackend.global.auth.dto.response.UserInfoResponse;
import com.factseekerbackend.domain.user.entity.CustomUserDetails;
import com.factseekerbackend.global.auth.jwt.JwtTokenProvider;
import com.factseekerbackend.global.auth.jwt.service.JwtService;
import com.factseekerbackend.global.exception.BusinessException;
import com.factseekerbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;

  @Transactional
  public LoginResponse login(LoginRequest loginRequest, String clientIP) {
    try {
      Authentication authentication = authenticateUser(loginRequest);
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

      String accessToken = jwtTokenProvider.createAccessToken(authentication);
      String refreshToken = jwtTokenProvider.createRefreshToken(authentication);
      jwtService.storeRefreshToken(userDetails.getUsername(), refreshToken);
      log.info("SECURITY_EVENT: LOGIN_SUCCESS - User: {}, IP: {}", userDetails.getUsername(), clientIP);

      User user = userRepository.findByLoginId(userDetails.getUsername())
          .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

      return LoginResponse.builder()
          .accessToken(accessToken)
          .refreshToken(refreshToken)
          .tokenType("Bearer")
          .success(true)
          .user(UserInfoResponse.from(user))
          .message("로그인 성공")
          .build();
    } catch (BadCredentialsException e) {
      log.warn("SECURITY_EVENT: LOGIN_FAILED - User: {}, IP: {}", loginRequest.getLoginId(), clientIP);
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }
  }

  private Authentication authenticateUser(LoginRequest loginRequest) {
    return authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequest.getLoginId(),
            loginRequest.getPassword()
        )
    );
  }

}
