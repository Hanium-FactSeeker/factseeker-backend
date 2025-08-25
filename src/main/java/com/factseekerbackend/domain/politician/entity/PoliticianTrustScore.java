package com.factseekerbackend.domain.politician.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "politician_trust_scores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PoliticianTrustScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "politician_id", nullable = false)
    private Politician politician;

    @Column(nullable = false)
    private LocalDate analysisDate;

    @Column(nullable = false, length = 20)
    private String analysisPeriod; // "2020-2025", "2024-01" 등

    // 종합 점수
    @Column(nullable = true)
    private Integer overallScore;

    // 세부 항목 점수
    @Column(nullable = true)
    private Integer integrityScore; // 정직성

    @Column(nullable = true)
    private Integer transparencyScore; // 투명성

    @Column(nullable = true)
    private Integer consistencyScore; // 일관성

    @Column(nullable = true)
    private Integer accountabilityScore; // 책임감

    // LLM별 점수
    @Column(nullable = true)
    private Integer gptScore;

    @Column(nullable = true)
    private Integer geminiScore;

    // GPT 근거
    @Column(columnDefinition = "TEXT")
    private String gptIntegrityReason;

    @Column(columnDefinition = "TEXT")
    private String gptTransparencyReason;

    @Column(columnDefinition = "TEXT")
    private String gptConsistencyReason;

    @Column(columnDefinition = "TEXT")
    private String gptAccountabilityReason;

    // Gemini 근거
    @Column(columnDefinition = "TEXT")
    private String geminiIntegrityReason;

    @Column(columnDefinition = "TEXT")
    private String geminiTransparencyReason;

    @Column(columnDefinition = "TEXT")
    private String geminiConsistencyReason;

    @Column(columnDefinition = "TEXT")
    private String geminiAccountabilityReason;

    // 분석 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnalysisStatus analysisStatus = AnalysisStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private Integer retryCount = 0;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @Builder
    public PoliticianTrustScore(Politician politician, LocalDate analysisDate, String analysisPeriod) {
        this.politician = politician;
        this.analysisDate = analysisDate;
        this.analysisPeriod = analysisPeriod;
        this.analysisStatus = AnalysisStatus.PENDING;
        this.retryCount = 0;
        // 점수 필드들은 updateScores 메서드에서 설정됨
    }

    public void updateScores(Integer overallScore, Integer integrityScore, Integer transparencyScore,
                           Integer consistencyScore, Integer accountabilityScore,
                           Integer gptScore, Integer geminiScore) {
        this.overallScore = overallScore;
        this.integrityScore = integrityScore;
        this.transparencyScore = transparencyScore;
        this.consistencyScore = consistencyScore;
        this.accountabilityScore = accountabilityScore;
        this.gptScore = gptScore;
        this.geminiScore = geminiScore;
        this.analysisStatus = AnalysisStatus.COMPLETED;
    }

    public void updateGPTReasons(String integrityReason, String transparencyReason,
                               String consistencyReason, String accountabilityReason) {
        this.gptIntegrityReason = integrityReason;
        this.gptTransparencyReason = transparencyReason;
        this.gptConsistencyReason = consistencyReason;
        this.gptAccountabilityReason = accountabilityReason;
    }

    public void updateGeminiReasons(String integrityReason, String transparencyReason,
                                  String consistencyReason, String accountabilityReason) {
        this.geminiIntegrityReason = integrityReason;
        this.geminiTransparencyReason = transparencyReason;
        this.geminiConsistencyReason = consistencyReason;
        this.geminiAccountabilityReason = accountabilityReason;
    }

    public void markAsFailed(String errorMessage) {
        this.analysisStatus = AnalysisStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public void resetRetryCount() {
        this.retryCount = 0;
    }

    public boolean canRetry() {
        return this.retryCount < 3;
    }

    public void setAnalysisDate(LocalDate analysisDate) {
        this.analysisDate = analysisDate;
    }

    public void setAnalysisPeriod(String analysisPeriod) {
        this.analysisPeriod = analysisPeriod;
    }
    
}
