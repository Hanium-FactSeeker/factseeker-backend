package com.factseekerbackend.domain.analysis.controller.dto.response;

import com.factseekerbackend.domain.analysis.entity.VideoAnalysis;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class VideoAnalysisResponse {
    private final String videoId;
    private final Integer totalConfidenceScore;
    private final String summary;
    private final String channelType;
    private final String channelTypeReason;
    private final String resultJson;
    private final LocalDateTime createdAt;

    @Builder
    public VideoAnalysisResponse(String videoId, Integer totalConfidenceScore, String summary, String channelType, String channelTypeReason, String resultJson, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.videoId = videoId;
        this.totalConfidenceScore = totalConfidenceScore;
        this.summary = summary;
        this.channelType = channelType;
        this.channelTypeReason = channelTypeReason;
        this.resultJson = resultJson;
        this.createdAt = createdAt;
    }

    public static VideoAnalysisResponse from(VideoAnalysis videoAnalysis){
        return VideoAnalysisResponse.builder()
                .videoId(videoAnalysis.getVideoId())
                .totalConfidenceScore(videoAnalysis.getTotalConfidenceScore())
                .summary(videoAnalysis.getSummary())
                .channelType(videoAnalysis.getChannelType())
                .channelTypeReason(videoAnalysis.getChannelTypeReason())
                .resultJson(videoAnalysis.getResultJson())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
