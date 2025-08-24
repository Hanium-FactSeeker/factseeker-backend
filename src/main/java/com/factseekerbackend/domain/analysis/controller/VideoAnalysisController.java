package com.factseekerbackend.domain.analysis.controller;

import com.factseekerbackend.domain.analysis.controller.dto.request.VideoUrlRequest;
import com.factseekerbackend.domain.analysis.controller.dto.request.VideoIdsRequest;
import com.factseekerbackend.domain.analysis.controller.dto.response.*;
import com.factseekerbackend.domain.analysis.controller.dto.response.fastapi.ClaimDto;
import com.factseekerbackend.domain.analysis.entity.Top10VideoAnalysis;
import com.factseekerbackend.domain.analysis.entity.VideoAnalysisStatus;
import com.factseekerbackend.domain.analysis.service.VideoAnalysisService;
import com.factseekerbackend.domain.analysis.service.fastapi.FactCheckTriggerService;
import com.factseekerbackend.domain.user.entity.CustomUserDetails;
import com.factseekerbackend.global.common.ApiResponse;
import com.factseekerbackend.global.exception.BusinessException;
import com.factseekerbackend.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.factseekerbackend.domain.analysis.repository.Top10VideoAnalysisRepository;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
@Tag(name = "비디오 분석", description = "유튜브 비디오 분석 및 팩트체크 API")
@Validated
public class VideoAnalysisController {

    private final FactCheckTriggerService factCheckTriggerService;
    private final VideoAnalysisService videoAnalysisService;
    private final Top10VideoAnalysisRepository top10VideoAnalysisRepository;
    private final ObjectMapper om;

    @Operation(
            summary = "비디오 분석 결과 조회",
            description = "특정 비디오 분석 ID에 대한 분석 결과를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "분석 결과 조회 성공",
                    content = @Content(schema = @Schema(implementation = VideoAnalysisResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "분석 결과를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/{videoAnalysisId}")
    public ResponseEntity<ApiResponse<VideoAnalysisResponse>> getVideoAnalysis(
            @Parameter(description = "비디오 분석 ID", example = "1")
            @PathVariable("videoAnalysisId") Long videoAnalysisId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = userDetails.getId();
            VideoAnalysisResponse videoAnalysis = videoAnalysisService.getVideoAnalysis(userId, videoAnalysisId);
            return ResponseEntity.ok(ApiResponse.success("비디오 분석 결과를 성공적으로 조회했습니다.", videoAnalysis));
        } catch (Exception e) {
            log.error("[API] 비디오 분석 결과 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("비디오 분석 결과 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "비디오 분석 요청",
            description = "유튜브 URL을 입력받아 비디오 분석을 요청합니다. 로그인 여부에 따라 다른 처리를 수행합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "분석 요청 성공",
                    content = @Content(schema = @Schema(oneOf = {String.class, VideoAnalysisResponse.class}))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping
    public CompletableFuture<ResponseEntity<ApiResponse<?>>> saveVideoAnalysis(
            @Parameter(description = "분석할 유튜브 URL", example = "{\"youtube_url\": \"https://www.youtube.com/watch?v=example\"}")
            @Valid @RequestBody VideoUrlRequest videoUrlRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[API] 비디오 분석 요청: {}", videoUrlRequest.youtubeUrl());
        String youtubeUrl = videoUrlRequest.youtubeUrl();
        try {
            if (userDetails != null) {
                Long userId = userDetails.getId();
                log.info("[API] 로그인 사용자 분석 요청 - User ID: {}", userId);
                Long analysisId = factCheckTriggerService.triggerAndReturnId(youtubeUrl, userId);
                return CompletableFuture.completedFuture(
                        ResponseEntity.ok(
                                ApiResponse.success(
                                        "비디오 분석이 성공적으로 요청되었습니다.",
                                        new AnalysisStartResponse(analysisId, VideoAnalysisStatus.PENDING)
                                )
                        )
                );
            } else {
                log.info("[API] 비로그인 사용자 분석 요청");
                return factCheckTriggerService.triggerSingleToRdsToNotLogin(youtubeUrl)
                        .thenApply(result -> ResponseEntity.ok(ApiResponse.success("비디오 분석 결과", result)));
            }
        } catch (Exception e) {
            log.error("[API] 비디오 분석 요청 실패: {}", e.getMessage());
            return CompletableFuture.completedFuture(
                    ResponseEntity.ok(ApiResponse.error("비디오 분석 요청에 실패했습니다: " + e.getMessage()))
            );
        }
    }


    @Operation(
            summary = "Top 10 비디오 분석 결과 조회",
            description = "특정 Top 10 비디오 ID에 대한 분석 결과를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "분석 결과 조회 성공",
                    content = @Content(schema = @Schema(implementation = VideoAnalysisResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "202",
                    description = "분석 진행 중",
                    content = @Content(schema = @Schema(implementation = VideoAnalysisResponse.class))
            )
    })
    @GetMapping("/top10/{videoId}")
    public ResponseEntity<VideoAnalysisResponse> getTop10VideoAnalysis(
            @Parameter(description = "비디오 ID", example = "exampleVideoId")
            @PathVariable("videoId") String videoId) {
        return top10VideoAnalysisRepository.findById(videoId)
                .map(analysis -> {
                    Object claims = parseClaims(analysis.getClaims());
                    return ResponseEntity.ok(VideoAnalysisResponse.from(analysis, claims));
                })
                .orElseGet(() -> ResponseEntity.accepted().body(VideoAnalysisResponse.builder()
                        .videoId(videoId)
                        .status(VideoAnalysisStatus.PENDING)
                        .build()));
    }

    @Operation(
            summary = "Top 10 유튜브 키워드 조회",
            description = "특정 Top 10 비디오 ID에 대한 키워드를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "키워드 조회 성공",
                    content = @Content(schema = @Schema(implementation = KeywordsResponse.class))
            )
    })
    @GetMapping("/top10/{videoId}/keywords")
    public ResponseEntity<ApiResponse<KeywordsResponse>> getTop10YoutubeKeywords(
            @Parameter(description = "비디오 ID", example = "exampleVideoId")
            @PathVariable String videoId) {
        return ResponseEntity.ok(ApiResponse.success("조회에 성공했습니다.", videoAnalysisService.getTop10YoutubeKeywords(videoId)));
    }

    private Object parseClaims(String claimsJson) {
        if (claimsJson == null || claimsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return om.readValue(claimsJson, new TypeReference<List<ClaimDto>>() {
            });
        } catch (JsonProcessingException e) {
            log.error("Failed to parse claims JSON in controller: {}", claimsJson, e);
            return Collections.emptyList();
        }
    }

    @GetMapping("/top10/{videoId}/percents")
    public ResponseEntity<ApiResponse<Top10AnalysisPercentResponse>> getVideoPercent(
            @PathVariable("videoId") String videoId
    ) {
        Top10VideoAnalysis analysis = top10VideoAnalysisRepository.findById(videoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VIDEO_NOT_FOUND));

        Top10AnalysisPercentResponse response = Top10AnalysisPercentResponse.from(analysis);

        return ResponseEntity.ok(ApiResponse.success("조회에 성공했습니다.", response));
    }

    @GetMapping("/top10/percents")
    public ResponseEntity<ApiResponse<List<Top10AnalysisPercentResponse>>> getVideosPercent(
            @RequestBody VideoIdsRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("조회에 성공했습니다.", videoAnalysisService.getTop10VideosPercent(request)));
    }
}
