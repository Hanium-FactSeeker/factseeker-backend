package com.factseekerbackend.domain.analysis.controller.dto.response;

import com.factseekerbackend.domain.analysis.entity.VideoAnalysisStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnalysisStartResponse {
    private final Long analysisId;
    private final VideoAnalysisStatus status;
}

