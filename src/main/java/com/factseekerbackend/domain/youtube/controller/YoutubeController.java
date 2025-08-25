package com.factseekerbackend.domain.youtube.controller;

import com.factseekerbackend.domain.youtube.controller.dto.response.YoutubeSearchResponse;
import com.factseekerbackend.domain.youtube.service.YoutubeService;
import com.factseekerbackend.global.exception.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.factseekerbackend.domain.youtube.controller.dto.response.VideoListResponse;
import com.factseekerbackend.domain.youtube.controller.dto.response.VideoItemResponse;
import com.factseekerbackend.domain.youtube.controller.dto.response.VideoDto;
import com.factseekerbackend.domain.youtube.service.PopularPoliticsService;
import com.factseekerbackend.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
                    content = @Content(
                            schema = @Schema(implementation = VideoListResponse.class),
                            examples = @ExampleObject(name = "성공 예시", value = "{\n  \"success\": true,\n  \"message\": \"인기 정치 영상을 성공적으로 조회했습니다.\",\n  \"data\": {\n    \"data\": [\n      {\n        \"videoId\": \"4zB4rPMgCuQ\",\n        \"videoTitle\": \"이 시각 정치 TOP1\",\n        \"thumbnailUrl\": \"https://i.ytimg.com/vi/4zB4rPMgCuQ/hqdefault.jpg\",\n        \"channelId\": \"UCxxxxxxx\",\n        \"channelTitle\": \"정치채널\"\n      },\n      {\n        \"videoId\": \"abcdEFGHijk\",\n        \"videoTitle\": \"이 시각 정치 TOP2\",\n        \"thumbnailUrl\": \"https://i.ytimg.com/vi/abcdEFGHijk/hqdefault.jpg\",\n        \"channelId\": \"UCyyyyyyy\",\n        \"channelTitle\": \"뉴스채널\"\n      }\n    ],\n    \"timestamp\": \"2025-07-08T09:12:34+09:00\"\n  }\n}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 오류",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(name = "실패 예시 (VIDEO_NOT_FOUND)", value = "{\n  \"success\": false,\n  \"message\": \"유효하지 않은 비디오ID 입니다.\"\n}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/videos")
    public ResponseEntity<ApiResponse<VideoListResponse>> popularPolitics(
            @Parameter(description = "조회할 영상 개수 (기본값: 10)", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            log.info("[API] 인기 정치 영상 조회 요청 - size: {}", size);
            VideoListResponse response = popularPoliticsService.getPopularPolitics(size);
            return ResponseEntity.ok(ApiResponse.success("인기 정치 영상을 성공적으로 조회했습니다.", response));
        } catch (Exception e) {
            log.error("[API] 인기 정치 영상 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(ErrorCode.VIDEO_NOT_FOUND.getStatus())
                    .body(ApiResponse.error(ErrorCode.VIDEO_NOT_FOUND.getMessage()));
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

    @Operation(
            summary = "랭크 기반 단건 인기 정치 영상 조회",
            description = "요청한 랭크에 해당하는 인기 정치 영상을 Redis 캐시에서 조회합니다. 캐시에 없으면 자동 갱신을 시도합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = VideoItemResponse.class),
                            examples = @ExampleObject(name = "성공 예시", value = "{\n  \"success\": true,\n  \"message\": \"인기 정치 영상 단건을 성공적으로 조회했습니다.\",\n  \"data\": {\n    \"data\": {\n      \"videoId\": \"4zB4rPMgCuQ\",\n      \"videoTitle\": \"이 시각 정치 TOP1\",\n      \"thumbnailUrl\": \"https://i.ytimg.com/vi/4zB4rPMgCuQ/hqdefault.jpg\",\n      \"channelId\": \"UCxxxxxxx\",\n      \"channelTitle\": \"정치채널\"\n    },\n    \"timestamp\": \"2025-07-08T09:12:34+09:00\",\n    \"rank\": 1\n  }\n}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 오류",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(name = "실패 예시 (VIDEO_NOT_FOUND)", value = "{\n  \"success\": false,\n  \"message\": \"유효하지 않은 비디오ID 입니다.\"\n}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/videos/{rank}")
    public ResponseEntity<ApiResponse<VideoItemResponse>> popularPoliticsByRank(
            @Parameter(description = "조회할 랭크 (1부터 시작)", example = "1")
            @PathVariable("rank") int rank
    ) {
        try {
            log.info("[API] 인기 정치 영상 단건 조회 - rank: {}", rank);
            VideoItemResponse item = popularPoliticsService.getPopularPoliticsByRank(rank);
            if (item == null) {
                log.error("[API] 인기 정치 영상 조회 실패: {}", "요청한 랭크의 영상 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(ErrorCode.VIDEO_NOT_FOUND.getStatus())
                        .body(ApiResponse.error(ErrorCode.VIDEO_NOT_FOUND.getMessage()));
            }
            return ResponseEntity.ok(ApiResponse.success("인기 정치 영상 단건을 성공적으로 조회했습니다.", item));
        } catch (Exception e) {
            log.error("[API] 인기 정치 영상 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(ErrorCode.VIDEO_NOT_FOUND.getStatus())
                    .body(ApiResponse.error(ErrorCode.VIDEO_NOT_FOUND.getMessage()));
        }
    }

    @Operation(
            summary = "비디오ID로 유튜브 영상 조회",
            description = "유튜브 Data API를 사용하여 videoId로 영상 정보를 단건 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = VideoDto.class),
                            examples = @ExampleObject(name = "성공 예시", value = "{\n  \"success\": true,\n  \"message\": \"유튜브 비디오 정보를 성공적으로 조회했습니다.\",\n  \"data\": {\n    \"videoId\": \"4zB4rPMgCuQ\",\n    \"videoTitle\": \"검색한 동영상 이름\",\n    \"thumbnailUrl\": \"https://i.ytimg.com/vi/4zB4rPMgCuQ/hqdefault.jpg\",\n    \"channelId\": \"UCxxxxxxx\",\n    \"channelTitle\": \"정치채널\"\n  }\n}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 오류",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(name = "실패 예시 (VIDEO_NOT_FOUND)", value = "{\n  \"success\": false,\n  \"message\": \"유효하지 않은 비디오ID 입니다.\"\n}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/video")
    public ResponseEntity<ApiResponse<VideoDto>> popularPoliticsByVideoId(
            @Parameter(description = "조회할 유튜브 비디오ID", example = "4zB4rPMgCuQ")
            @RequestParam("videoId") String videoId
    ) {
        try {
            log.info("[API] 유튜브 비디오 단건 조회 - videoId: {}", videoId);
            if (videoId == null || videoId.isBlank()) {
                throw new IllegalArgumentException("videoId must not be empty");
            }
            VideoDto dto = youtubeService.getVideoById(videoId);
            if (dto == null) {
                log.error("[API] 유튜브 비디오 조회 실패: {}", "요청한 비디오ID의 영상 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(ErrorCode.VIDEO_NOT_FOUND.getStatus())
                        .body(ApiResponse.error(ErrorCode.VIDEO_NOT_FOUND.getMessage()));
            }
            return ResponseEntity.ok(ApiResponse.success("유튜브 비디오 정보를 성공적으로 조회했습니다.", dto));
        } catch (Exception e) {
            log.error("[API] 유튜브 비디오 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(ErrorCode.VIDEO_NOT_FOUND.getStatus())
                    .body(ApiResponse.error(ErrorCode.VIDEO_NOT_FOUND.getMessage()));
        }
    }
}
