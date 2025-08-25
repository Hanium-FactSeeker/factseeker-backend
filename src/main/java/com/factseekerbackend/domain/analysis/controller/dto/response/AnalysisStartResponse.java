package com.factseekerbackend.domain.analysis.controller.dto.response;

import com.factseekerbackend.domain.analysis.entity.AnalysisStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "로그인 사용자 분석 접수 응답")
public class AnalysisStartResponse {
    @Schema(description = "분석 식별자", example = "123")
    private final Long analysisId;

    @Schema(description = "현재 상태", example = "PENDING")
    private final AnalysisStatus status;
}
