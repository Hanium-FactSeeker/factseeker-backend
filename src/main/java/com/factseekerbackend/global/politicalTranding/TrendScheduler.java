package com.factseekerbackend.global.politicalTranding;

import com.factseekerbackend.global.politicalTranding.dto.TrendApiResponse;
import com.factseekerbackend.global.politicalTranding.service.TrendService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class TrendScheduler {

  @Value("${fastapi.trends-url}")
  private String trendsUrl;

  private WebClient webClient;
  private final TrendService trendService;

  public TrendScheduler(TrendService trendService) {
    this.trendService = trendService;
  }

  @PostConstruct
  public void init() {
    this.webClient = WebClient.builder()
        .baseUrl(trendsUrl)
        .build();
  }

  @Scheduled(fixedRate = 600000)
  public void fetchTrendsFromPython() {
    log.info("Fetching trends from Python API...");

    webClient.get()
        .uri("/api/trends")
        .retrieve()
        .bodyToMono(TrendApiResponse.class)
        .doOnError(error -> log.error("Failed to fetch trends from Python API: {}", error.getMessage()))
        .subscribe(response -> trendService.updateTrends(response.getTrends()));
  }
}
