package com.factseekerbackend.domain.analysis.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "진행률/상태 집계 응답")
public class PercentStatusData {
    @Schema(description = "요청된 ID 수", example = "2")
    private int requested;

    @Schema(description = "완료 수", example = "1")
    private int completed;

    @Schema(description = "대기 수", example = "1")
    private int pending;

    @Schema(description = "실패 수", example = "0")
    private int failed;

    @Schema(description = "미존재 수", example = "0")
    private int notFound;

    @Schema(description = "ID별 상세 상태 리스트")
    private List<PercentStatusResponse> results;
}
