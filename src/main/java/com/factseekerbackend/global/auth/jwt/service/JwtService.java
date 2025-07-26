package com.factseekerbackend.global.auth.jwt.service;

import com.factseekerbackend.global.auth.dto.LoginRequest;
import com.factseekerbackend.global.auth.dto.LoginResponse;
import com.factseekerbackend.global.auth.dto.UserInfo;
import com.factseekerbackend.global.auth.jwt.JwtTokenProvider;
import com.factseekerbackend.global.auth.jwt.dto.TokenRefreshResponse;
import com.factseekerbackend.global.exception.InvalidTokenException;
import com.factseekerbackend.global.auth.service.CustomUserDetails;
import com.factseekerbackend.global.auth.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

  private final JwtTokenProvider jwtTokenProvider;
  private final AuthenticationManager authenticationManager;
  private final RedisTemplate<String, String> redisTemplate;
  private final CustomUserDetailsService customUserDetailsService;

  public LoginResponse login(LoginRequest loginRequest) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getLoginId(),
            loginRequest.getPassword()));

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

    String accessToken = jwtTokenProvider.createAccessToken(authentication);
    String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

    storeRefreshToken(userDetails.getUsername(), refreshToken);
    logLoginActivity(userDetails);

    return LoginResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .user(UserInfo.from(userDetails))
        .build();
  }

  public String extractTokenFromRequest(HttpServletRequest request) throws InvalidTokenException {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    throw new InvalidTokenException("No token provided");
  }

  public TokenRefreshResponse refreshAccessToken(String refreshToken)
      throws InvalidTokenException {
    if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
      throw new InvalidTokenException("유효하지 않은 리프레시 토큰입니다.");
    }

    String loginId = jwtTokenProvider.getUsernameFromToken(refreshToken);

    if (!isValidStoredRefreshToken(loginId, refreshToken)) {
      throw new InvalidTokenException("리프레시 토큰은 찾지 못했거나 만료되었습니다.");
    }

    CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(
        loginId);

//    if (!userDetails.isAccountNonLocked()) {
//      throw new AccountLockedException("계정이 잠겼습니다.");
//    }
//    if (!userDetails.isEnabled()) {
//      throw new DisabledException("계정을 사용할 수 없습니다.");
//    }

    Authentication newAuth = new UsernamePasswordAuthenticationToken(userDetails, null,
        userDetails.getAuthorities());

    String newAccessToken = jwtTokenProvider.createAccessToken(newAuth);

    return TokenRefreshResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(refreshToken)
        .user(UserInfo.from(userDetails))
        .build();
  }

  public void logout(String accessToken) {
    String loginId = jwtTokenProvider.getUsernameFromToken(accessToken);

    CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(
        loginId);

    removeRefreshToken(loginId);
    logLogoutActivity(userDetails);
  }

  public UserInfo getUserInfo(String accessToken) throws InvalidTokenException {
    if (!jwtTokenProvider.validateAccessToken(accessToken)) {
      throw new InvalidTokenException("유효하지 않은 토큰입니다.");
    }

    Claims claims = jwtTokenProvider.getClaims(accessToken);
    String loginId = jwtTokenProvider.getUsernameFromToken(accessToken);

    return UserInfo.builder()
        .loginId(loginId)
        .roles(extractRoles(claims))
        .build();
  }

  private List<String> extractRoles(Claims claims) {
    @SuppressWarnings("unchecked")
    List<String> roles = (List<String>) claims.get("roles");
    return roles != null ? roles : List.of();
  }

  private void storeRefreshToken(String loginId, String refreshToken) {
    String key = "refresh_token:" + loginId;
    Duration expiration = Duration.ofDays(7);
    redisTemplate.opsForValue().set(key, refreshToken, expiration);
  }

  private boolean isValidStoredRefreshToken(String loginId, String token) {
    String key = "refresh_token:" + loginId;
    String storedToken = redisTemplate.opsForValue().get(key);
    return token.equals(storedToken);
  }

  private void removeRefreshToken(String loginId) {
    String key = "refresh_token:" + loginId;
    redisTemplate.delete(key);
  }

  private void logLoginActivity(CustomUserDetails userDetails) {
    log.info("User login: {} ({}), Roles: {}",
        userDetails.getUsername(),
        userDetails.getEmail(),
        userDetails.getAuthorities());
  }

  private void logLogoutActivity(CustomUserDetails userDetails) {
    log.info("User logout: {} ({})",
        userDetails.getUsername(),
        userDetails.getEmail());
  }

}
