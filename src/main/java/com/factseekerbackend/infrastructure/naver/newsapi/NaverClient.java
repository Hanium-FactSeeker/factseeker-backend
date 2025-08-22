package com.factseekerbackend.infrastructure.naver.newsapi;

import com.factseekerbackend.infrastructure.naver.newsapi.dto.request.NewsRequest;
import com.factseekerbackend.infrastructure.naver.newsapi.dto.response.NewsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class NaverClient {

    @Qualifier("naverRestClient")
    private final RestClient naverRestClient;

    /** 네이버 뉴스 검색 */
    public NewsResponse search(String query, int display, int start, String sort) {
        // 생성자 기반 불변 DTO 사용 (검증은 생성자 내부에서 수행)
        NewsRequest req = new NewsRequest(query, display, start, sort);

        return naverRestClient.get()
                .uri(b -> b.path("/v1/search/news.json")
                        .queryParams(req.toMultiValueMap())
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                                .onStatus(HttpStatusCode::is4xxClientError, (r, resp) -> {
                    throw new IllegalArgumentException("Naver 4xx: " + resp.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (r, resp) -> {
                    throw new IllegalStateException("Naver 5xx: " + resp.getStatusCode());
                })
                .body(NewsResponse.class);
    }
}