package com.factseekerbackend.domain.politician.controller.dto.response;

import com.factseekerbackend.domain.politician.entity.AnalysisStatus;
import com.factseekerbackend.domain.politician.entity.PoliticianTrustScore;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class TrustScoreResponse {

    private Long id;
    private Long politicianId;
    private String politicianName;
    private LocalDate analysisDate;
    private String analysisPeriod;
    
    private Integer overallScore;
    private Integer integrityScore;
    private Integer transparencyScore;
    private Integer consistencyScore;
    private Integer accountabilityScore;
    
    private Integer gptScore;
    private Integer geminiScore;
    
    // GPT 근거
    private String gptIntegrityReason;
    private String gptTransparencyReason;
    private String gptConsistencyReason;
    private String gptAccountabilityReason;
    
    // Gemini 근거
    private String geminiIntegrityReason;
    private String geminiTransparencyReason;
    private String geminiConsistencyReason;
    private String geminiAccountabilityReason;
    
    private AnalysisStatus analysisStatus;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime lastUpdated;

    public static TrustScoreResponse from(PoliticianTrustScore trustScore) {
        return TrustScoreResponse.builder()
                .id(trustScore.getId())
                .politicianId(trustScore.getPolitician().getId())
                .politicianName(trustScore.getPolitician().getName())
                .analysisDate(trustScore.getAnalysisDate())
                .analysisPeriod(trustScore.getAnalysisPeriod())
                .overallScore(trustScore.getOverallScore())
                .integrityScore(trustScore.getIntegrityScore())
                .transparencyScore(trustScore.getTransparencyScore())
                .consistencyScore(trustScore.getConsistencyScore())
                .accountabilityScore(trustScore.getAccountabilityScore())
                .gptScore(trustScore.getGptScore())
                .geminiScore(trustScore.getGeminiScore())
                // GPT 근거
                .gptIntegrityReason(trustScore.getGptIntegrityReason())
                .gptTransparencyReason(trustScore.getGptTransparencyReason())
                .gptConsistencyReason(trustScore.getGptConsistencyReason())
                .gptAccountabilityReason(trustScore.getGptAccountabilityReason())
                // Gemini 근거
                .geminiIntegrityReason(trustScore.getGeminiIntegrityReason())
                .geminiTransparencyReason(trustScore.getGeminiTransparencyReason())
                .geminiConsistencyReason(trustScore.getGeminiConsistencyReason())
                .geminiAccountabilityReason(trustScore.getGeminiAccountabilityReason())
                .analysisStatus(trustScore.getAnalysisStatus())
                .errorMessage(trustScore.getErrorMessage())
                .retryCount(trustScore.getRetryCount())
                .lastUpdated(trustScore.getLastUpdated())
                .build();
    }
}
