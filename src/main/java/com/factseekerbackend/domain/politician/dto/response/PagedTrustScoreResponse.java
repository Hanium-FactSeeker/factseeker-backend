package com.factseekerbackend.domain.politician.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "페이지네이션된 신뢰도 점수 응답")
public record PagedTrustScoreResponse(
    @Schema(description = "신뢰도 점수 목록")
    List<TrustScoreResponse> trustScores,
    
    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    int currentPage,
    
    @Schema(description = "페이지 크기", example = "10")
    int pageSize,
    
    @Schema(description = "전체 요소 수", example = "30")
    long totalElements,
    
    @Schema(description = "전체 페이지 수", example = "3")
    int totalPages,
    
    @Schema(description = "첫 번째 페이지 여부", example = "true")
    boolean isFirst,
    
    @Schema(description = "마지막 페이지 여부", example = "false")
    boolean isLast
) {}
