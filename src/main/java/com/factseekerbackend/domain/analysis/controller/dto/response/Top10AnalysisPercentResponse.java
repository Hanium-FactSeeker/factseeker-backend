package com.factseekerbackend.domain.analysis.controller.dto.response;

import com.factseekerbackend.domain.analysis.entity.video.Top10VideoAnalysis;

public record Top10AnalysisPercentResponse(Integer totalConfidenceScore) {
    public static Top10AnalysisPercentResponse from(Top10VideoAnalysis videoAnalysis) {
        return new Top10AnalysisPercentResponse(videoAnalysis.getTotalConfidenceScore());
    }
}
