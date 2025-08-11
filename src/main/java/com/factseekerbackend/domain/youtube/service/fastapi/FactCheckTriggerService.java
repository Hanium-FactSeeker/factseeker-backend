package com.factseekerbackend.domain.youtube.service.fastapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactCheckTriggerService {

    private final RestClient fastApiClient;           // RestClientConfig에서 주입 (base-url)
    private final FactCheckResultService resultService; // RDS UPSERT 서비스

    /**
     * FastAPI POST 호출 → 응답 JSON을 RDS에 UPSERT.
     * Redis 사용 안 함.
     */
    public void triggerSingleToRds(String videoId) {
        if (videoId == null || videoId.isBlank()) return;

        // videoId만 들어오면 전체 URL로 변환
        String youtubeUrl = videoId.startsWith("http")
                ? videoId
                : "https://www.youtube.com/watch?v=" + videoId;

        int maxAttempts = 3;
        long backoffMs = 200L;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                String response = fastApiClient.post()
                        .uri("/fact-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", videoId)
                        .header("X-Requested-By", "spring-cron")
                        .body(Map.of("youtube_url", youtubeUrl))   // ★ JSON 바디로 전달
                        .retrieve()
                        .body(String.class);

                // ▼ FastAPI 응답 JSON을 그대로 RDS에 저장 (요약 컬럼 + result_json)
                resultService.upsertFromFastApiResponse(response);

                log.info("✅ FastAPI 처리 성공 → RDS 저장 완료 videoId={} (attempt {}/{})",
                        videoId, attempt, maxAttempts);
                return;

            } catch (Exception e) {
                log.warn("⚠️ FastAPI 처리 실패 videoId={} (attempt {}/{}): {}",
                        videoId, attempt, maxAttempts, e.toString());
                try { Thread.sleep(backoffMs); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                backoffMs *= 2;
            }
        }
    }
}
