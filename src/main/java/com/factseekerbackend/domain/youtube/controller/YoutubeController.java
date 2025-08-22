package com.factseekerbackend.domain.youtube.controller;

import com.factseekerbackend.domain.youtube.controller.dto.response.YoutubeSearchResponse;
import com.factseekerbackend.domain.youtube.service.YoutubeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.factseekerbackend.domain.youtube.controller.dto.response.VideoListResponseDto;
import com.factseekerbackend.domain.youtube.service.PopularPoliticsService;
import com.factseekerbackend.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
@Tag(name = "유튜브 관리", description = "유튜브 인기 정치 영상 조회 및 검색 API")
public class YoutubeController {

    private final PopularPoliticsService popularPoliticsService;
    private final YoutubeService youtubeService;

    @Operation(
            summary = "인기 정치 영상 조회",
            description = "현재 인기 있는 정치 관련 유튜브 영상을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = VideoListResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/videos")
    public ResponseEntity<ApiResponse<VideoListResponseDto>> popularPolitics(
            @Parameter(description = "조회할 영상 개수 (기본값: 10)", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            log.info("[API] 인기 정치 영상 조회 요청 - size: {}", size);
            VideoListResponseDto response = popularPoliticsService.getPopularPolitics(size);
            return ResponseEntity.ok(ApiResponse.success("인기 정치 영상을 성공적으로 조회했습니다.", response));
        } catch (Exception e) {
            log.error("[API] 인기 정치 영상 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("인기 정치 영상 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "유튜브 영상 검색",
            description = "키워드로 유튜브 영상을 검색합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "검색 성공",
                    content = @Content(schema = @Schema(implementation = YoutubeSearchResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<YoutubeSearchResponse>>> search(
            @Parameter(description = "검색할 키워드", example = "정치")
            @RequestParam("keyword") String keyword
    ) throws IOException {
        log.info("[API] 유튜브 영상 검색 요청 - keyword: {}", keyword);
        List<YoutubeSearchResponse> response = youtubeService.searchVideos(keyword);
        return ResponseEntity.ok(ApiResponse.success("유튜브 영상 검색을 성공적으로 조회했습니다.", response));
    }
}