package com.factseekerbackend.domain.youtube.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Top10FactCheckResult {
    @Id
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

    /** 전체 FastAPI 응답 JSON 원문 보관 (claims 포함) */
    @Column(name = "result_json", columnDefinition = "JSON")
    private String resultJson;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

