package com.factseekerbackend.domain.analysis.controller.dto.response;

import com.factseekerbackend.domain.analysis.entity.Top10VideoAnalysis;

public record AnalysisPercentResponse(Integer totalConfidenceScore) {
    public static AnalysisPercentResponse from(Top10VideoAnalysis videoAnalysis) {
        return new AnalysisPercentResponse(videoAnalysis.getTotalConfidenceScore());
    }
}
