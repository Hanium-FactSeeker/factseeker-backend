package com.factseekerbackend.global.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
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

  private SecretKey key;

  @PostConstruct
  protected void init() {
    if (secretKey == null || secretKey.trim().isEmpty()) {
      throw new IllegalArgumentException("JWT secret key cannot be null or empty");
    }

    if (accessTokenExpiration <= 0 || refreshTokenExpiration <= 0) {
      throw new IllegalArgumentException("Token expiration time must be positive");
    }

    this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    log.info("JWT TokenProvider initialized successfully");
  }

  public String createAccessToken(Authentication authentication) {
    String username = authentication.getName();

    String authorities = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

    Date now = new Date();
    Date validity = new Date(now.getTime() + accessTokenExpiration);

    return Jwts.builder()
        .setSubject(username) // JWT의 subject
        .claim("auth", authorities) // "auth" 클레임에 권한 정보 저장
        .claim("tye", "access")
        .setIssuedAt(now) // 토큰 발행 시간
        .setExpiration(validity) // 토큰 만료 시간
        .signWith(key, SignatureAlgorithm.HS256) // 서명
        .compact();
  }

  public String createRefreshToken(Authentication authentication) {
    String username = authentication.getName();

    Date now = new Date();
    Date validity = new Date(now.getTime() + refreshTokenExpiration);

    return Jwts.builder()
        .setSubject(username)
        .claim("tye", "refresh")
        .setIssuedAt(now)
        .setExpiration(validity)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public Authentication getAuthentication(String token) {
    Claims claims = getClaims(token);

    Object authClaim = claims.get("auth");
    Collection<? extends GrantedAuthority> authorities;

    if (authClaim != null && !authClaim.toString().trim().isEmpty()) {
      authorities = Arrays.stream(authClaim.toString().split(","))
          .filter(auth -> !auth.trim().isEmpty()) // 빈 문자열 필터링
          .map(SimpleGrantedAuthority::new)
          .collect(Collectors.toList());
    } else {
      authorities = Collections.emptyList();
    }

    UserDetails principal = new User(claims.getSubject(), "", authorities);
    return new UsernamePasswordAuthenticationToken(principal, token, authorities);
  }

  public boolean validateToken(String token) {
    try {
      Claims claims = getClaims(token);

      String tokenType = (String) claims.get("type");
      String subject = claims.getSubject();

      log.debug("Token validation successful - Type: {}, Subject: {}, Expires: {}",
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

  public String getUsernameFromToken(String token) {
    Claims claims = getClaims(token);
    return claims.getSubject();
  }

  public Claims getClaims(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public String getTokenType(String token) {
    try {
      Claims claims = getClaims(token);
      return (String) claims.get("type");
    } catch (Exception e) {
      log.warn("토큰의 타입을 가져오는데 실패했습니다.", e);
      return null;
    }
  }

  public boolean isAccessToken(String token) {
    return "access".equals(getTokenType(token));
  }

  public boolean isRefreshToken(String token) {
    return "refresh".equals(getTokenType(token));
  }

  public boolean validateAccessToken(String token) {
    return validateToken(token) && isAccessToken(token);
  }

  public boolean validateRefreshToken(String token) {
    return validateToken(token) && isRefreshToken(token);
  }

}
