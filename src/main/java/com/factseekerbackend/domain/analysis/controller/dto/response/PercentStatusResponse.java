package com.factseekerbackend.domain.analysis.controller.dto.response;

import com.factseekerbackend.domain.analysis.entity.AnalysisStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "ID별 상태 응답")
public class PercentStatusResponse {
    @Schema(description = "비디오 ID", example = "abc123")
    private String videoId;

    @Schema(description = "상태", example = "COMPLETED")
    private AnalysisStatus status;

    @Schema(description = "완료된 경우의 총점", example = "78")
    private Integer totalConfidenceScore;

    @Schema(description = "부가 메시지", example = "해당 ID는 Top 10 목록에 없습니다.")
    private String message;

    public PercentStatusResponse(String videoId) {
        this.videoId = videoId;
    }

    public PercentStatusResponse status(AnalysisStatus status) {
        this.status = status;
        return this;
    }

    public PercentStatusResponse totalConfidenceScore(Integer totalConfidenceScore) {
        this.totalConfidenceScore = totalConfidenceScore;
        return this;
    }

    public PercentStatusResponse message(String message) {
        this.message = message;
        return this;
    }
}
