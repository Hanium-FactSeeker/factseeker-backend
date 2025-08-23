package com.factseekerbackend.domain.politician.service;

import com.factseekerbackend.domain.politician.dto.LLMAnalysisResult;
import com.factseekerbackend.domain.politician.entity.AnalysisStatus;
import com.factseekerbackend.domain.politician.entity.Politician;
import com.factseekerbackend.domain.politician.entity.PoliticianTrustScore;
import com.factseekerbackend.domain.politician.repository.PoliticianRepository;
import com.factseekerbackend.domain.politician.repository.PoliticianTrustScoreRepository;
import com.factseekerbackend.domain.politician.service.llm.LLMAnalysisService;
import com.factseekerbackend.domain.politician.service.llm.LLMServiceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import com.factseekerbackend.domain.politician.service.llm.impl.GPTAnalysisService;
import com.factseekerbackend.domain.politician.service.llm.impl.GeminiAnalysisService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PoliticianTrustAnalysisService {

    private final PoliticianRepository politicianRepository;
    private final PoliticianTrustScoreRepository trustScoreRepository;
    private final GPTAnalysisService gptAnalysisService;
    private final GeminiAnalysisService geminiAnalysisService;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(2); // 3 -> 2로 변경

    /**
     * 모든 정치인에 대한 신뢰도를 분석을 수행합니다.
     */
    @Async
    public void analyzeAllPoliticians() {
        log.info("[BATCH] 정치인 신뢰도 분석 배치 시작");
        
        List<Politician> politicians = politicianRepository.findAll();
        LocalDate analysisDate = LocalDate.now();
        String analysisPeriod = "2020-2025";
        
        for (int i = 0; i < politicians.size(); i++) {
            Politician politician = politicians.get(i);
            log.info("[ANALYSIS] {} 분석 시작 ({}/{})", politician.getName(), i + 1, politicians.size());
            
            try {
                analyzePoliticianTrustScore(politician, analysisDate, analysisPeriod);
                log.info("[ANALYSIS] {} 분석 완료 - 최종점수: {}", politician.getName(), 
                    getLatestScore(politician.getId()));
                
                // Gemini API rate limiting 방지를 위한 대기 시간 추가
                if (i < politicians.size() - 1) { // 마지막 정치인이 아닌 경우
                    try {
                        Thread.sleep(2000); // 2초 대기
                        log.info("[BATCH] 다음 분석을 위해 2초 대기 중...");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("[BATCH] 대기 시간이 중단되었습니다.");
                    }
                }
                
            } catch (Exception e) {
                log.error("[ANALYSIS] {} 분석 실패: {}", politician.getName(), e.getMessage());
                // 개별 정치인 분석 실패는 전체 배치를 중단하지 않음
            }
        }
        
        log.info("[BATCH] 정치인 신뢰도 분석 배치 완료");
    }

    /**
     * 특정 정치인의 신뢰도를 분석합니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void analyzePoliticianTrustScore(Politician politician, LocalDate analysisDate, String analysisPeriod) {
        // 기존 분석 결과 확인
        PoliticianTrustScore existingScore = trustScoreRepository.findByPoliticianIdAndAnalysisDate(
                politician.getId(), analysisDate).orElse(null);
        
        if (existingScore != null) {
            // 기존 분석이 있는 경우, 실패한 LLM만 재분석
            if (existingScore.getAnalysisStatus() == AnalysisStatus.COMPLETED) {
                log.info("[ANALYSIS] {} 이미 완료된 분석 존재 - 실패한 LLM만 재분석", politician.getName());
                analyzeFailedLLMsOnly(politician, existingScore, analysisPeriod);
                return;
            } else if (existingScore.getAnalysisStatus() == AnalysisStatus.FAILED) {
                log.info("[ANALYSIS] {} 기존 분석이 실패 상태 - 전체 재분석", politician.getName());
                // 실패한 분석은 삭제하고 새로 시작
                trustScoreRepository.delete(existingScore);
            }
        }

        try {
            // 2개 LLM 동시 분석 실행
            List<CompletableFuture<LLMAnalysisResult>> futures = List.of(
                    CompletableFuture.supplyAsync(() -> gptAnalysisService.analyzeTrustScore(politician, analysisPeriod), executorService),
                    CompletableFuture.supplyAsync(() -> geminiAnalysisService.analyzeTrustScore(politician, analysisPeriod), executorService)
            );

            // 모든 분석 완료 대기
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 결과 수집 및 점수 계산
            List<LLMAnalysisResult> results = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            // 성공한 분석 결과만 필터링
            List<LLMAnalysisResult> successfulResults = results.stream()
                    .filter(LLMAnalysisResult::isSuccess)
                    .toList();

            if (successfulResults.isEmpty()) {
                // 분석 실패 시 실패 상태로 저장
                PoliticianTrustScore trustScore = PoliticianTrustScore.builder()
                        .politician(politician)
                        .analysisDate(analysisDate)
                        .analysisPeriod(analysisPeriod)
                        .build();
                trustScore.markAsFailed("모든 LLM 분석이 실패했습니다.");
                trustScoreRepository.save(trustScore);
                return;
            }

            // 점수 계산
            int totalIntegrity = 0, totalTransparency = 0, totalConsistency = 0, totalAccountability = 0;
            int validCount = 0;

            // LLM별 점수 초기화 (실패 시 null)
            Integer gptScore = null;
            Integer geminiScore = null;

            for (LLMAnalysisResult result : successfulResults) {
                if (result.getIntegrityScore() != null) {
                    totalIntegrity += result.getIntegrityScore();
                    totalTransparency += result.getTransparencyScore();
                    totalConsistency += result.getConsistencyScore();
                    totalAccountability += result.getAccountabilityScore();
                    validCount++;
                }
            }

            // LLM별 점수 설정 (각 서비스 타입에 따라)
            for (int i = 0; i < results.size(); i++) {
                LLMAnalysisResult result = results.get(i);
                LLMServiceType serviceType = null;
                if (i == 0) {
                    serviceType = LLMServiceType.GPT;
                } else if (i == 1) {
                    serviceType = LLMServiceType.GEMINI;
                }
                
                if (result.isSuccess() && result.getOverallScore() != null) {
                    switch (serviceType) {
                        case GPT:
                            gptScore = result.getOverallScore();
                            break;
                        case GEMINI:
                            geminiScore = result.getOverallScore();
                            break;
                    }
                }
            }

            if (validCount > 0) {
                int avgIntegrity = totalIntegrity / validCount;
                int avgTransparency = totalTransparency / validCount;
                int avgConsistency = totalConsistency / validCount;
                int avgAccountability = totalAccountability / validCount;
                int overallScore = (avgIntegrity + avgTransparency + avgConsistency + avgAccountability) / 4;

                // 분석 결과 엔티티 생성 및 점수 설정
                PoliticianTrustScore trustScore = PoliticianTrustScore.builder()
                        .politician(politician)
                        .analysisDate(analysisDate)
                        .analysisPeriod(analysisPeriod)
                        .build();

                // 점수 업데이트
                trustScore.updateScores(overallScore, avgIntegrity, avgTransparency, avgConsistency, avgAccountability,
                        gptScore, geminiScore);

                // 각 LLM별 근거 업데이트
                for (int i = 0; i < results.size(); i++) {
                    LLMAnalysisResult result = results.get(i);
                    LLMServiceType serviceType = null;
                    if (i == 0) {
                        serviceType = LLMServiceType.GPT;
                    } else if (i == 1) {
                        serviceType = LLMServiceType.GEMINI;
                    }
                    
                    if (result.isSuccess()) {
                        switch (serviceType) {
                            case GPT:
                                trustScore.updateGPTReasons(
                                    result.getIntegrityReason(),
                                    result.getTransparencyReason(),
                                    result.getConsistencyReason(),
                                    result.getAccountabilityReason()
                                );
                                break;
                            case GEMINI:
                                trustScore.updateGeminiReasons(
                                    result.getIntegrityReason(),
                                    result.getTransparencyReason(),
                                    result.getConsistencyReason(),
                                    result.getAccountabilityReason()
                                );
                                break;
                        }
                    }
                }

                // 데이터베이스에 저장
                trustScoreRepository.save(trustScore);

                log.info("[ANALYSIS] {} 분석 완료 - 종합점수: {}", politician.getName(), overallScore);
            } else {
                // 유효한 결과가 없는 경우 실패 상태로 저장
                PoliticianTrustScore trustScore = PoliticianTrustScore.builder()
                        .politician(politician)
                        .analysisDate(analysisDate)
                        .analysisPeriod(analysisPeriod)
                        .build();
                trustScore.markAsFailed("유효한 분석 결과가 없습니다.");
                trustScoreRepository.save(trustScore);
            }

        } catch (Exception e) {
            log.error("[ANALYSIS] {} 분석 중 오류 발생: {}", politician.getName(), e.getMessage());
            // 오류 발생 시 실패 상태로 저장
            PoliticianTrustScore trustScore = PoliticianTrustScore.builder()
                    .politician(politician)
                    .analysisDate(analysisDate)
                    .analysisPeriod(analysisPeriod)
                    .build();
            trustScore.markAsFailed("분석 중 오류 발생: " + e.getMessage());
            trustScoreRepository.save(trustScore);
        }
    }

    /**
     * 완료된 분석에서 실패한 LLM만 재분석합니다.
     */
    private void analyzeFailedLLMsOnly(Politician politician, PoliticianTrustScore existingScore, String analysisPeriod) {
        try {
            List<LLMAnalysisResult> results = new ArrayList<>();
            List<LLMServiceType> failedServices = new ArrayList<>();
            
            // 실패한 LLM 서비스 식별 (GPT와 Gemini만)
            if (existingScore.getGptScore() == null) {
                failedServices.add(LLMServiceType.GPT);
            }
            if (existingScore.getGeminiScore() == null) {
                failedServices.add(LLMServiceType.GEMINI);
            }
            
            if (failedServices.isEmpty()) {
                log.info("[ANALYSIS] {} 모든 LLM 분석 완료 - 재분석 불필요", politician.getName());
                return;
            }
            
            log.info("[ANALYSIS] {} 실패한 LLM 재분석 시작: {}", politician.getName(), failedServices);
            
            // 실패한 LLM만 재분석
            for (LLMServiceType serviceType : failedServices) {
                LLMAnalysisService service = null;
                if (serviceType == LLMServiceType.GPT) {
                    service = gptAnalysisService;
                } else if (serviceType == LLMServiceType.GEMINI) {
                    service = geminiAnalysisService;
                }
                
                if (service != null) {
                    LLMAnalysisResult result = service.analyzeTrustScore(politician, analysisPeriod);
                    results.add(result);
                    
                    if (result.isSuccess()) {
                        // 성공한 결과를 기존 엔티티에 업데이트
                        updateExistingScoreWithLLMResult(existingScore, result, serviceType);
                        log.info("[ANALYSIS] {} {} 재분석 성공", politician.getName(), serviceType);
                    } else {
                        log.warn("[ANALYSIS] {} {} 재분석 실패: {}", politician.getName(), serviceType, result.getErrorMessage());
                    }
                }
            }
            
            // 업데이트된 엔티티 저장
            trustScoreRepository.save(existingScore);
            
        } catch (Exception e) {
            log.error("[ANALYSIS] {} 실패한 LLM 재분석 중 오류: {}", politician.getName(), e.getMessage());
        }
    }

    /**
     * 기존 분석 결과에 특정 LLM의 결과를 업데이트합니다.
     */
    private void updateExistingScoreWithLLMResult(PoliticianTrustScore existingScore, LLMAnalysisResult result, LLMServiceType serviceType) {
        switch (serviceType) {
            case GPT:
                existingScore.updateGPTReasons(
                    result.getIntegrityReason(),
                    result.getTransparencyReason(),
                    result.getConsistencyReason(),
                    result.getAccountabilityReason()
                );
                break;
            case GEMINI:
                existingScore.updateGeminiReasons(
                    result.getIntegrityReason(),
                    result.getTransparencyReason(),
                    result.getConsistencyReason(),
                    result.getAccountabilityReason()
                );
                break;
        }
        
        // 점수도 업데이트
        existingScore.updateScores(
            existingScore.getOverallScore(),
            existingScore.getIntegrityScore(),
            existingScore.getTransparencyScore(),
            existingScore.getConsistencyScore(),
            existingScore.getAccountabilityScore(),
            existingScore.getGptScore() != null ? existingScore.getGptScore() : 
                (serviceType == LLMServiceType.GPT ? result.getOverallScore() : existingScore.getGptScore()),
            existingScore.getGeminiScore() != null ? existingScore.getGeminiScore() : 
                (serviceType == LLMServiceType.GEMINI ? result.getOverallScore() : existingScore.getGeminiScore())
        );
    }

    /**
     * 실패한 분석을 재시도합니다.
     */
    public void retryFailedAnalyses() {
        log.info("[RETRY] 실패한 분석 재시도 시작");
        
        List<PoliticianTrustScore> failedScores = trustScoreRepository.findFailedScoresForRetry();
        
        for (PoliticianTrustScore trustScore : failedScores) {
            if (trustScore.canRetry()) {
                log.info("[RETRY] {} 재시도 ({}번째)", trustScore.getPolitician().getName(), trustScore.getRetryCount() + 1);
                
                try {
                    trustScore.incrementRetryCount();
                    analyzePoliticianTrustScore(trustScore.getPolitician(), trustScore.getAnalysisDate(), trustScore.getAnalysisPeriod());
                } catch (Exception e) {
                    log.error("[RETRY] {} 재시도 실패: {}", trustScore.getPolitician().getName(), e.getMessage());
                }
            } else {
                log.warn("[RETRY] {} 최대 재시도 횟수 초과", trustScore.getPolitician().getName());
            }
        }
        
        log.info("[RETRY] 실패한 분석 재시도 완료");
    }

    /**
     * 정치인의 최신 점수를 조회합니다.
     */
    private Integer getLatestScore(Long politicianId) {
        return trustScoreRepository.findLatestCompletedScore(politicianId)
                .map(PoliticianTrustScore::getOverallScore)
                .orElse(null);
    }


}
