package com.factseekerbackend.domain.youtube.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularCacheRefresher {

    private final PopularPoliticsService popularPoliticsService;

    @Scheduled(cron = "0 0 0/6 * * *", zone = "Asia/Seoul")
    public void refreshPopularPolitics() {
        try {
            // Top10을 규칙적으로 갱신 (Redis 해시 + 동일 timestamp)
            popularPoliticsService.refreshTopN(10);
        } catch (Exception e) {
            // 스케줄러 스레드에 예외가 전파되지 않도록 로깅 후 종료
            log.warn("Popular politics cache refresh failed (will retry next schedule)", e);
        }
    }
}
