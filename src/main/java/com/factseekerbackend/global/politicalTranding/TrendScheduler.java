package com.factseekerbackend.global.politicalTranding;

import com.factseekerbackend.global.politicalTranding.dto.TrendApiResponse;
import com.factseekerbackend.global.politicalTranding.service.TrendService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class TrendScheduler {

  private final WebClient webClient;
  private final TrendService trendService;

  public TrendScheduler(TrendService trendService) {
    this.trendService = trendService;
    this.webClient = WebClient.builder()
        .baseUrl("http://localhost:5001") // 파이썬 API 서버 주소
        .build();
  }

  @PostConstruct
  public void initTrendsData() {
    log.info("Initializing trends data at application startup...");
    fetchTrendsFromPython();
  }

  @Scheduled(fixedRate = 600000)
  public void fetchTrendsFromPython() {
    log.info("Fetching trends from Python API...");

    webClient.get()
        .uri("/api/trends") // 파이썬 API 엔드포인트
        .retrieve()
        .bodyToMono(TrendApiResponse.class)
        .doOnError(error -> log.error("Failed to fetch trends from Python API: {}", error.getMessage()))
        .subscribe(response -> trendService.updateTrends(response.getTrends()));
  }

}
