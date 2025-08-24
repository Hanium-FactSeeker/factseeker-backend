package com.factseekerbackend.domain.analysis.entity;

import com.factseekerbackend.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Top10VideoAnalysis {
    @Id
    @Column(name = "video_id", length = 32, nullable = false)
    private String videoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private VideoAnalysisStatus status;

    @Column(name = "video_url", length = 255)
    private String videoUrl;

    @Column(name = "total_confidence_score")
    private Integer totalConfidenceScore;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "channel_type", length = 50)
    private String channelType;

    @Column(name = "channel_type_reason", columnDefinition = "TEXT")
    private String channelTypeReason;

    @Column(name = "claims", columnDefinition = "JSON")
    private String claims;

    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "three_line_summary", columnDefinition = "TEXT")
    private String threeLineSummary;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

