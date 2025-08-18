package com.factseekerbackend.domain.history.dto.response;

import com.factseekerbackend.domain.analysis.entity.VideoAnalysisStatus;
import com.factseekerbackend.domain.history.entity.AnalysisHistory;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record HistoryResponse(
    Long historyId,
    String videoId,
    String videoTitle,
    String thumbnailUrl,
    VideoAnalysisStatus status,
    LocalDateTime createdAt
) {

    public static HistoryResponse from(AnalysisHistory history) {
        return HistoryResponse.builder()
                .historyId(history.getId())
                .videoId(history.getVideoId())
                .videoTitle(history.getVideoTitle())
                .thumbnailUrl(history.getThumbnailUrl())
                .status(history.getVideoAnalysis().getStatus()) // VideoAnalysis 객체에서 상태를 가져옴
                .createdAt(history.getCreatedAt())
                .build();
    }
}
