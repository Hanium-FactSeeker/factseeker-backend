package com.factseekerbackend.domain.politician.dto;

import com.factseekerbackend.domain.politician.entity.Politician;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LLMAnalysisResult {

    private Politician politician;
    private Integer overallScore;
    private Integer integrityScore;
    private Integer transparencyScore;
    private Integer consistencyScore;
    private Integer accountabilityScore;
    
    private String integrityReason;
    private String transparencyReason;
    private String consistencyReason;
    private String accountabilityReason;
    
    private String analysisSummary;
    private String errorMessage;
    private boolean isSuccess;
}
