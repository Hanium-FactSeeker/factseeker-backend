package com.factseekerbackend.global.auth.controller;

import com.factseekerbackend.global.auth.service.AuthService;
import com.factseekerbackend.global.auth.dto.request.LoginRequest;
import com.factseekerbackend.global.auth.dto.request.LoginResponse;
import com.factseekerbackend.global.auth.dto.request.SocialUserInfoResponse;
import com.factseekerbackend.global.auth.dto.response.UserInfoResponse;
import com.factseekerbackend.global.auth.jwt.dto.request.TokenRefreshRequest;
import com.factseekerbackend.global.auth.jwt.dto.response.TokenRefreshResponse;
import com.factseekerbackend.global.auth.jwt.service.JwtService;
import com.factseekerbackend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증 관리", description = "사용자 인증 및 토큰 관리 API")
public class AuthController {

    private final JwtService jwtService;
    private final AuthService authService;

    @Operation(
        summary = "소셜 토큰 검증",
        description = "소셜 로그인 후 추가 정보 입력을 위한 임시 토큰을 검증합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "토큰 검증 성공",
            content = @Content(schema = @Schema(implementation = SocialUserInfoResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 토큰",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @GetMapping("/social/verify")
    public ResponseEntity<ApiResponse<SocialUserInfoResponse>> verifySocialToken(
            @Parameter(description = "검증할 임시 토큰", example = "temp_token_123")
            @RequestParam String token) {
        try {
            SocialUserInfoResponse response = jwtService.verifySocialToken(token);
            return ResponseEntity.ok(ApiResponse.success("소셜 토큰 검증이 완료되었습니다.", response));
        } catch (Exception e) {
            log.error("[API] 소셜 토큰 검증 실패: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("토큰 검증에 실패했습니다: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "사용자 로그인",
        description = "아이디와 비밀번호를 사용하여 사용자 로그인을 수행합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 로그인 정보",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Parameter(description = "로그인 요청 정보")
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        try {
            String clientIP = extractClientIP(request);
            LoginResponse response = authService.login(loginRequest, clientIP);
            return ResponseEntity.ok(ApiResponse.success("로그인이 성공적으로 완료되었습니다.", response));
        } catch (Exception e) {
            log.error("[API] 로그인 실패: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("로그인에 실패했습니다: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "토큰 새로고침",
        description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "토큰 새로고침 성공",
            content = @Content(schema = @Schema(implementation = TokenRefreshResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 리프레시 토큰",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(
            @Parameter(description = "토큰 새로고침 요청 정보")
            @RequestBody TokenRefreshRequest refreshTokenRequest,
            HttpServletRequest httpRequest) {
        try {
            String clientIP = extractClientIP(httpRequest);
            TokenRefreshResponse response = jwtService.refreshAccessToken(
                refreshTokenRequest.getRefreshToken(), clientIP);
            return ResponseEntity.ok(ApiResponse.success("토큰이 성공적으로 새로고침되었습니다.", response));
        } catch (Exception e) {
            log.error("[API] 토큰 새로고침 실패: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("토큰 새로고침에 실패했습니다: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "사용자 로그아웃",
        description = "현재 사용자의 토큰을 무효화하여 로그아웃을 수행합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        try {
            String clientIP = extractClientIP(request);
            String token = jwtService.extractTokenFromRequest(request);
            jwtService.logout(token, clientIP);
            return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다."));
        } catch (Exception e) {
            log.error("[API] 로그아웃 실패: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("로그아웃 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "현재 사용자 정보 조회",
        description = "현재 로그인된 사용자의 정보를 조회합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "사용자 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = UserInfoResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 요청",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser(HttpServletRequest request) {
        try {
            String token = jwtService.extractTokenFromRequest(request);
            UserInfoResponse userInfoResponse = jwtService.getUserInfo(token);
            return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회 성공", userInfoResponse));
        } catch (Exception e) {
            log.error("[API] 사용자 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("사용자 정보 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    private String extractClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }

        String xrHeader = request.getHeader("X-Real-IP");
        if (xrHeader != null && !xrHeader.isEmpty()) {
            return xrHeader;
        }

        return request.getRemoteAddr();
    }
}
