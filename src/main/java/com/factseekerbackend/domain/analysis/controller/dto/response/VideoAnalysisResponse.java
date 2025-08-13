package com.factseekerbackend.domain.analysis.controller.dto.response;

import com.factseekerbackend.domain.analysis.entity.VideoAnalysis;
import com.factseekerbackend.domain.analysis.entity.Top10VideoAnalysis;
import com.factseekerbackend.domain.analysis.entity.VideoAnalysisStatus; // Add this import
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
    private final VideoAnalysisStatus status;

    @Builder
    public VideoAnalysisResponse(String videoId, Integer totalConfidenceScore, String summary, String channelType, String channelTypeReason, String resultJson, LocalDateTime createdAt, VideoAnalysisStatus status) {
        this.videoId = videoId;
        this.totalConfidenceScore = totalConfidenceScore;
        this.summary = summary;
        this.channelType = channelType;
        this.channelTypeReason = channelTypeReason;
        this.resultJson = resultJson;
        this.createdAt = createdAt;
        this.status = status;
    }

    public static VideoAnalysisResponse from(VideoAnalysis videoAnalysis){
        return VideoAnalysisResponse.builder()
                .videoId(videoAnalysis.getVideoId())
                .totalConfidenceScore(videoAnalysis.getTotalConfidenceScore())
                .summary(videoAnalysis.getSummary())
                .channelType(videoAnalysis.getChannelType())
                .channelTypeReason(videoAnalysis.getChannelTypeReason())
                .resultJson(videoAnalysis.getResultJson())
                .createdAt(videoAnalysis.getCreatedAt())
                .status(videoAnalysis.getStatus())
                .build();
    }

    public static VideoAnalysisResponse from(Top10VideoAnalysis top10VideoAnalysis){
        return VideoAnalysisResponse.builder()
                .videoId(top10VideoAnalysis.getVideoId())
                .totalConfidenceScore(top10VideoAnalysis.getTotalConfidenceScore())
                .summary(top10VideoAnalysis.getSummary())
                .channelType(top10VideoAnalysis.getChannelType())
                .channelTypeReason(top10VideoAnalysis.getChannelTypeReason())
                .resultJson(top10VideoAnalysis.getResultJson())
                .createdAt(top10VideoAnalysis.getCreatedAt())
                .status(VideoAnalysisStatus.COMPLETED)
                .build();
    }
}
