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
    private final String videoUrl;
    private final Integer totalConfidenceScore;
    private final String summary;
    private final String channelType;
    private final String channelTypeReason;
    private final Object claims; // Can be String (from DB) or List<ClaimDto> (from fresh analysis)
    private final String keywords;
    private final String threeLineSummary;
    private final LocalDateTime createdAt;
    private final VideoAnalysisStatus status;

    @Builder
    public VideoAnalysisResponse(String videoId, String videoUrl, Integer totalConfidenceScore, String summary, String channelType, String channelTypeReason, Object claims, String keywords, String threeLineSummary, LocalDateTime createdAt, VideoAnalysisStatus status) {
        this.videoId = videoId;
        this.videoUrl = videoUrl;
        this.totalConfidenceScore = totalConfidenceScore;
        this.summary = summary;
        this.channelType = channelType;
        this.channelTypeReason = channelTypeReason;
        this.claims = claims;
        this.keywords = keywords;
        this.threeLineSummary = threeLineSummary;
        this.createdAt = createdAt;
        this.status = status;
    }

    public static VideoAnalysisResponse from(VideoAnalysis videoAnalysis, Object claims) {
        return VideoAnalysisResponse.builder()
                .videoId(videoAnalysis.getVideoId())
                .videoUrl(videoAnalysis.getVideoUrl())
                .totalConfidenceScore(videoAnalysis.getTotalConfidenceScore())
                .summary(videoAnalysis.getSummary())
                .channelType(videoAnalysis.getChannelType())
                .channelTypeReason(videoAnalysis.getChannelTypeReason())
                .claims(claims)
                .keywords(videoAnalysis.getKeywords())
                .threeLineSummary(videoAnalysis.getThreeLineSummary())
                .createdAt(videoAnalysis.getCreatedAt())
                .status(videoAnalysis.getStatus())
                .build();
    }

    public static VideoAnalysisResponse from(Top10VideoAnalysis top10VideoAnalysis, Object claims) {
        return VideoAnalysisResponse.builder()
                .videoId(top10VideoAnalysis.getVideoId())
                .videoUrl(top10VideoAnalysis.getVideoUrl())
                .totalConfidenceScore(top10VideoAnalysis.getTotalConfidenceScore())
                .summary(top10VideoAnalysis.getSummary())
                .channelType(top10VideoAnalysis.getChannelType())
                .channelTypeReason(top10VideoAnalysis.getChannelTypeReason())
                .claims(claims)
                .keywords(top10VideoAnalysis.getKeywords())
                .threeLineSummary(top10VideoAnalysis.getThreeLineSummary())
                .createdAt(top10VideoAnalysis.getCreatedAt())
                .status(VideoAnalysisStatus.COMPLETED)
                .build();
    }
}
