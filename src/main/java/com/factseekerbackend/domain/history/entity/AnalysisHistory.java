package com.factseekerbackend.domain.history.entity;

import com.factseekerbackend.domain.analysis.entity.VideoAnalysis;
import com.factseekerbackend.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("ANALYSIS")
public class AnalysisHistory extends History {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_analysis_id")
    private VideoAnalysis videoAnalysis;

    @Column(nullable = false)
    private String videoId;

    @Column(nullable = false)
    private String videoTitle;

    private String thumbnailUrl;

    @Builder
    public AnalysisHistory(User user, VideoAnalysis videoAnalysis, String videoId, String videoTitle, String thumbnailUrl) {
        super(user);
        this.videoAnalysis = videoAnalysis;
        this.videoId = videoId;
        this.videoTitle = videoTitle;
        this.thumbnailUrl = thumbnailUrl;
    }
}
