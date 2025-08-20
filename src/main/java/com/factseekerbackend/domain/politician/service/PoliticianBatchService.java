package com.factseekerbackend.domain.politician.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PoliticianBatchService {

    private final PoliticianTrustAnalysisService analysisService;

    /**
     * 매월 1일 새벽 2시에 자동으로 정치인 신뢰도 분석을 실행합니다.
     */
    @Scheduled(cron = "0 0 2 1 * *") // 매월 1일 새벽 2시
    public void monthlyTrustAnalysis() {
        log.info("[SCHEDULER] 월간 정치인 신뢰도 분석 스케줄러 시작");
        
        try {
            analysisService.analyzeAllPoliticians();
            log.info("[SCHEDULER] 월간 정치인 신뢰도 분석 완료");
        } catch (Exception e) {
            log.error("[SCHEDULER] 월간 정치인 신뢰도 분석 실패: {}", e.getMessage());
        }
    }

    /**
     * 매일 새벽 3시에 실패한 분석을 재시도합니다.
     */
    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    public void retryFailedAnalyses() {
        log.info("[SCHEDULER] 실패한 분석 재시도 스케줄러 시작");
        
        try {
            analysisService.retryFailedAnalyses();
            log.info("[SCHEDULER] 실패한 분석 재시도 완료");
        } catch (Exception e) {
            log.error("[SCHEDULER] 실패한 분석 재시도 실패: {}", e.getMessage());
        }
    }


}
