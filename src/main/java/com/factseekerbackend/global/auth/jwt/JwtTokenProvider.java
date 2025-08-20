package com.factseekerbackend.global.auth.jwt;

import com.factseekerbackend.domain.user.entity.CustomUserDetails;
import com.factseekerbackend.domain.user.entity.User;
import com.factseekerbackend.domain.user.repository.UserRepository;
import com.factseekerbackend.global.exception.BusinessException;
import com.factseekerbackend.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

  @Value("${jwt.secret}")
  private String secretKey;
  @Value("${jwt.access-token-expiration-milliseconds}")
  private long accessTokenExpiration;
  @Value("${jwt.refresh-token-expiration-milliseconds}")
  private long refreshTokenExpiration;

  private final UserRepository userRepository;
  private SecretKey key;

  public JwtTokenProvider(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @PostConstruct
  protected void init() {
    validateConfiguration();
    this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    log.info("JWT TokenProvider initialized - Access: {}ms, Refresh: {}ms",
        accessTokenExpiration, refreshTokenExpiration);
  }

  public String createAccessToken(Authentication authentication) {
    return createToken(authentication, accessTokenExpiration, TokenType.ACCESS);
  }

  public String createRefreshToken(Authentication authentication) {
    return createToken(authentication, refreshTokenExpiration, TokenType.REFRESH);
  }

  public String createTempToken(String loginId) {
    Date now = new Date();
    long tempTokenExpiration = 600000; // 10분
    Date validity = new Date(now.getTime() + tempTokenExpiration);

    return Jwts.builder()
        .setSubject(loginId)
        .claim("type", TokenType.TEMP.getValue())
        .setIssuedAt(now)
        .setExpiration(validity)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public Authentication getAuthentication(String token) {
    try {
      Claims claims = getClaims(token);
      String username = claims.getSubject();

      User userEntity;

      try {
        Long userId = Long.parseLong(username);
        userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
      } catch (NumberFormatException e) {
        userEntity = userRepository.findByLoginId(username)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
      }

      CustomUserDetails principal = CustomUserDetails.create(userEntity);
      return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    } catch (Exception e) {
      log.error("Authentication 생성 실패: {}", e.getMessage());
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }
  }

  public String getUsernameFromToken(String token) {
    return getClaims(token).getSubject();
  }

  public boolean validateAccessToken(String token) {
    return validateToken(token) && isTokenType(token, TokenType.ACCESS);
  }

  public boolean validateRefreshToken(String token) {
    return validateToken(token) && isTokenType(token, TokenType.REFRESH);
  }

  public Claims getClaims(String token) {
    try {
      return Jwts.parser()
          .verifyWith(key)
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (Exception e) {
      log.error("토큰 파싱 실패: {}", e.getMessage());
      throw new BusinessException(ErrorCode.TOKEN_PARSING_FAILED);
    }
  }

  public Long getExpiration(String token) {
    try {
      Date expiration = getClaims(token).getExpiration();
      Date now = new Date();
      return expiration.after(now) ? expiration.getTime() - now.getTime() : 0L;
    } catch (ExpiredJwtException e) {
      return 0L;
    } catch (Exception e) {
      log.warn("토큰 만료 시간 조회 실패: {}", e.getMessage());
      return 0L;
    }
  }

  private void validateConfiguration() {
    if (secretKey == null || secretKey.trim().isEmpty()) {
      throw new IllegalArgumentException("JWT secret key cannot be null or empty");
    }
    if (accessTokenExpiration <= 0 || refreshTokenExpiration <= 0) {
      throw new IllegalArgumentException("Token expiration time must be positive");
    }
    if (accessTokenExpiration >= refreshTokenExpiration) {
      throw new IllegalArgumentException(
          "Access token expiration must be less than refresh token expiration");
    }
  }

  private boolean validateToken(String token) {
    try {
      Claims claims = getClaims(token);
      String tokenType = (String) claims.get("type");
      String subject = claims.getSubject();

      if (subject == null || subject.trim().isEmpty()) {
        log.warn("토큰에 유효한 subject가 없습니다");
        return false;
      }

      log.debug("토큰 유효성 검증 성공 - Type: {}, Subject: {}, Expires: {}",
          tokenType, subject, claims.getExpiration());

      return true;
    } catch (SecurityException | MalformedJwtException e) {
      log.warn("유효하지 않은 JWT 서명입니다.", e);
    } catch (ExpiredJwtException e) {
      log.warn("만료된 JWT 토큰입니다.", e);
    } catch (IllegalArgumentException e) {
      log.warn("JWT 토큰의 형식이 잘못되었거나 손상되었습니다.", e);
    } catch (Exception e) {
      log.warn("JWT 토큰 검증 중에 오류가 발생했습니다.", e);
    }
    return false;
  }

  private boolean isTokenType(String token, TokenType expectedType) {
    try {
      Claims claims = getClaims(token);
      String tokenType = (String) claims.get("type");
      return expectedType.getValue().equals(tokenType);
    } catch (Exception e) {
      log.warn("토큰 타입 확인 실패: {}", e.getMessage());
      return false;
    }
  }

  private String createToken(Authentication authentication, long expiration, TokenType type) {
    String username = authentication.getName();
    List<String> authorities = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority).collect(Collectors.toList());

    Date now = new Date();
    Date validity = new Date(now.getTime() + expiration);

    JwtBuilder builder = Jwts.builder()
        .setSubject(username)
        .claim("type", type.getValue())
        .setIssuedAt(now)
        .setExpiration(validity)
        .signWith(key, SignatureAlgorithm.HS256);

    if (type == TokenType.ACCESS) {
      builder.claim("roles", authorities);
    }

    return builder.compact();
  }

}
