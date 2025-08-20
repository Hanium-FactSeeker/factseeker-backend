package com.factseekerbackend.domain.youtube.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PopularCacheRefresher {

    private final PopularPoliticsService popularPoliticsService;

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void refreshPopularPolitics() {
        // Top10을 규칙적으로 갱신 (Redis 해시 + 동일 timestamp)
        popularPoliticsService.refreshTopN(10);
    }
}
