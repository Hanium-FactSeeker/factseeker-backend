package com.factseekerbackend.global.politicalTranding.controller;

import com.factseekerbackend.global.common.ApiResponse;
import com.factseekerbackend.global.politicalTranding.service.TrendService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TrendController {

  private final TrendService trendService;

  @GetMapping("/latest-trends")
  public ResponseEntity<ApiResponse<List<String>>> getLatestTrends() {
    List<String> latestTrends = trendService.getLatestTrends();

    return ResponseEntity.ok(ApiResponse.success("최신 트렌드 순위를 성공적으로 조회했습니다.", latestTrends));
  }

}
