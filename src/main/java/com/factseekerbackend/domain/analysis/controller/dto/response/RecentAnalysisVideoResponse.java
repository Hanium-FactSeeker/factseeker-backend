package com.factseekerbackend.domain.analysis.controller.dto.response;

import com.factseekerbackend.domain.analysis.entity.video.VideoAnalysis;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecentAnalysisVideoResponse {

    private final Long videoAnalysisId;
    private final String videoUrl;
    private final String videoTitle;

    public static RecentAnalysisVideoResponse from(VideoAnalysis videoAnalysis, String title) {

        return RecentAnalysisVideoResponse.builder()
                .videoAnalysisId(videoAnalysis.getId())
                .videoUrl(videoAnalysis.getVideoUrl())
                .videoTitle(title)
                .build();
    }
}
