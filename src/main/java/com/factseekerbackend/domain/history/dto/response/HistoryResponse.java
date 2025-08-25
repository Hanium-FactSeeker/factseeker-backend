package com.factseekerbackend.domain.history.dto.response;

import com.factseekerbackend.domain.analysis.entity.AnalysisStatus;
import com.factseekerbackend.domain.history.entity.AnalysisHistory;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record HistoryResponse(
    Long historyId,
    String videoId,
    String videoTitle,
    String thumbnailUrl,
    AnalysisStatus status,
    LocalDateTime createdAt
) {


}
