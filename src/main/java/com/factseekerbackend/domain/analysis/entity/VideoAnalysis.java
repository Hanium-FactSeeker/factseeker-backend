package com.factseekerbackend.domain.analysis.entity;

import com.factseekerbackend.domain.user.entity.User;
import com.factseekerbackend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
public class VideoAnalysis extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "video_analysis_id")
    private Long id;

    @Column(name = "video_id", length = 32, nullable = false)
    private String videoId;

    @Column(name = "total_confidence_score")
    private Integer totalConfidenceScore;

    // 코드 기준 키 이름은 summary (예시 JSON의 confidence_summary 아님)
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "channel_type", length = 50)
    private String channelType;

    @Column(name = "channel_type_reason", columnDefinition = "TEXT")
    private String channelTypeReason;

    @Column(name = "result_json", columnDefinition = "JSON")
    private String resultJson;

    @Enumerated(EnumType.STRING) // EnumType.STRING으로 저장
    @Column(name = "status", length = 50)
    private VideoAnalysisStatus status; // 타입 변경

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
