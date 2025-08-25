package com.factseekerbackend.domain.history.entity;

import com.factseekerbackend.domain.analysis.entity.video.VideoAnalysis;
import com.factseekerbackend.domain.user.entity.User;
import com.factseekerbackend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long analysisHistoryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_analysis_id", unique = true)
    private VideoAnalysis videoAnalysis;
}
