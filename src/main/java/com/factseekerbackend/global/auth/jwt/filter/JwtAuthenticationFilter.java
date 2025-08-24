package com.factseekerbackend.global.auth.jwt.filter;

import com.factseekerbackend.global.auth.jwt.JwtTokenProvider;
import com.factseekerbackend.global.auth.jwt.service.JwtService;
import com.factseekerbackend.global.exception.BusinessException;
import com.factseekerbackend.global.exception.InvalidTokenException;
import com.factseekerbackend.global.exception.ErrorCode;
import org.springframework.security.core.AuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    if (shouldNotFilter(request)) {
      filterChain.doFilter(request, response);
      return;
    }
    String token = null;
    try {
      token = jwtService.extractTokenFromRequest(request);

      if (token != null) {
        if (jwtService.isTokenBlacklisted(token)) {
          log.warn("블랙리스트에 등록된 토큰입니다: {}", token);
          throw new InvalidTokenException("블랙리스트에 등록된 토큰입니다.");
        }

        if (token.split("\\.").length != 3) {
          log.warn("토큰 형식이 올바르지 않습니다: {}", token);
          throw new InvalidTokenException("유효하지 않은 토큰 형식입니다.");
        }

        if (jwtTokenProvider.validateAccessToken(token)) {
          Authentication authentication = jwtTokenProvider.getAuthentication(token);
          SecurityContextHolder.getContext().setAuthentication(authentication);
          log.debug("인증 정보 설정 완료: {}", authentication.getName());
        } else {
          log.warn("유효하지 않은 액세스 토큰입니다.");
          SecurityContextHolder.clearContext();
          response.setStatus(ErrorCode.INVALID_CREDENTIALS.getStatus().value());
          response.setContentType("application/json;charset=UTF-8");
          response.setCharacterEncoding("UTF-8");
          if (!response.isCommitted()) {
            response.getWriter().write("{\"success\":false,\"message\":\"" + ErrorCode.INVALID_CREDENTIALS.getMessage() + "\"}");
            response.getWriter().flush();
          }
          return;
        }
      }
    } catch (BusinessException e) {
      log.warn("JWT 인증 실패: {}", e.getMessage());
      org.springframework.security.core.context.SecurityContextHolder.clearContext();
      response.setStatus(e.getErrorCode().getStatus().value());
      response.setContentType("application/json;charset=UTF-8");
      response.setCharacterEncoding("UTF-8");
      String message = e.getErrorCode() != null ? e.getErrorCode().getMessage() : ErrorCode.INVALID_CREDENTIALS.getMessage();
      if (!response.isCommitted()) {
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
        response.getWriter().flush();
      }
      return;
    } catch (InvalidTokenException e) {
      log.warn("JWT 인증 실패: {}", e.getMessage());
      org.springframework.security.core.context.SecurityContextHolder.clearContext();
      response.setStatus(ErrorCode.INVALID_CREDENTIALS.getStatus().value());
      response.setContentType("application/json;charset=UTF-8");
      response.setCharacterEncoding("UTF-8");
      if (!response.isCommitted()) {
        response.getWriter().write("{\"success\":false,\"message\":\"" + ErrorCode.INVALID_CREDENTIALS.getMessage() + "\"}");
        response.getWriter().flush();
      }
      return;
    } catch (Exception e) {
      log.error("JWT 필터 처리 중 예외 발생: {}", e.getMessage(), e);
      org.springframework.security.core.context.SecurityContextHolder.clearContext();
      response.setStatus(ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value());
      response.setContentType("application/json;charset=UTF-8");
      response.setCharacterEncoding("UTF-8");
      if (!response.isCommitted()) {
        response.getWriter().write("{\"success\":false,\"message\":\"" + ErrorCode.INTERNAL_SERVER_ERROR.getMessage() + "\"}");
        response.getWriter().flush();
      }
      return;
    }

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/api/auth/login") ||
        path.startsWith("/api/auth/register") ||
        path.startsWith("/api/auth/refresh") ||
        path.startsWith("/api/social") ||
        path.startsWith("/api/check") ||
        path.startsWith("/oauth2/") ||
        path.startsWith("/api/youtube/") ||
        path.startsWith("/swagger-ui") ||
        path.startsWith("/api-docs") ||
        path.startsWith("/v3/api-docs") ||
        path.startsWith("/api/trends") ||
        path.startsWith("/api/politicians") ||
        path.startsWith("/api/latest-trends") ||
        path.startsWith("/api/news");
  }

}
