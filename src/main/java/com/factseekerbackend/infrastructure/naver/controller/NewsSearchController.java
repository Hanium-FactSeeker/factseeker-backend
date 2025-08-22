package com.factseekerbackend.infrastructure.naver.controller;

import com.factseekerbackend.global.common.ApiResponse;
import com.factseekerbackend.infrastructure.naver.newsapi.NaverClient;
import com.factseekerbackend.infrastructure.naver.newsapi.dto.response.NewsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/news")
@RestController
public class NewsSearchController {
    private final NaverClient naverClient;

    @GetMapping
    public ResponseEntity<ApiResponse<NewsResponse>> searchNews(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "1") int start,
            @RequestParam(required = false, defaultValue = "10") int display,
            @RequestParam(required = false, defaultValue = "sim") String sort
    ) {

        NewsResponse response = naverClient.search(query, display, start, sort);

        return ResponseEntity.ok(ApiResponse.success("뉴스 정보 조회 성공.", response));
    }
}
