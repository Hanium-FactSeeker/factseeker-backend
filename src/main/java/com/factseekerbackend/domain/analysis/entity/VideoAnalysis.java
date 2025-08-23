package com.factseekerbackend.domain.analysis.entity;

import com.factseekerbackend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
public class VideoAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "video_analysis_id")
    private Long id;

    @Column(name = "video_id", length = 225, nullable = false)
    private String videoId;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private VideoAnalysisStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
