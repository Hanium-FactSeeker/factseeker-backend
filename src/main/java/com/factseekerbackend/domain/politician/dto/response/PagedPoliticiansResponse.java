package com.factseekerbackend.domain.politician.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "페이지네이션된 정치인 목록 응답")
public record PagedPoliticiansResponse(
    @Schema(description = "정치인 목록")
    List<PoliticianResponse> politicians,
    
    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    int currentPage,
    
    @Schema(description = "페이지 크기", example = "10")
    int pageSize,
    
    @Schema(description = "전체 요소 수", example = "50")
    long totalElements,
    
    @Schema(description = "전체 페이지 수", example = "5")
    int totalPages,
    
    @Schema(description = "첫 번째 페이지 여부", example = "true")
    boolean isFirst,
    
    @Schema(description = "마지막 페이지 여부", example = "false")
    boolean isLast
) {}
