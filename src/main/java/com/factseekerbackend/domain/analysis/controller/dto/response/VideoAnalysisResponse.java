package com.factseekerbackend.domain.analysis.controller.dto.response;

import com.factseekerbackend.domain.analysis.entity.VideoAnalysis;
import com.factseekerbackend.domain.analysis.entity.Top10VideoAnalysis;
import com.factseekerbackend.domain.analysis.entity.VideoAnalysisStatus; // Add this import
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "비디오 분석 결과 응답")
public class VideoAnalysisResponse {
    @Schema(description = "유튜브 비디오 ID", example = "abc123")
    private final String videoId;

    @Schema(description = "유튜브 전체 URL", example = "https://www.youtube.com/watch?v=abc123")
    private final String videoUrl;

    @Schema(description = "신뢰도 총점(0~100)", example = "78")
    private final Integer totalConfidenceScore;

    @Schema(description = "영상 요약", example = "영상에서 다룬 핵심 내용을 요약합니다.")
    private final String summary;

    @Schema(description = "채널 유형", example = "뉴스")
    private final String channelType;

    @Schema(description = "채널 유형 판별 근거", example = "주요 업로드가 시사 뉴스를 다룸")
    private final String channelTypeReason;

    @Schema(description = "주장/검증 리스트. 내부적으로 List<ClaimDto>이나 저장값에 따라 문자열일 수 있습니다.", example = "[]")
    private final Object claims; // Can be String (from DB) or List<ClaimDto> (from fresh analysis)

    @Schema(description = "쉼표로 구분된 키워드 문자열", example = "선거,경제,국회")
    private final String keywords;

    @Schema(description = "3줄 요약", example = "이 동영상은 ~")
    private final String threeLineSummary;

    @Schema(description = "생성 시각", example = "2025-01-01T12:34:56")
    private final LocalDateTime createdAt;

    @Schema(description = "분석 상태", example = "COMPLETED")
    private final VideoAnalysisStatus status;

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
