package com.factseekerbackend.domain.analysis.service.fastapi;

import com.factseekerbackend.domain.analysis.entity.VideoAnalysis;
import com.factseekerbackend.domain.analysis.repository.VideoAnalysisRepository;
import com.factseekerbackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import com.factseekerbackend.domain.analysis.entity.VideoAnalysisStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactCheckTriggerService {

    private final RestClient fastApiClient;           // RestClientConfig에서 주입 (base-url)
    private final FactCheckResultService resultService; // RDS UPSERT 서비스
    private final VideoAnalysisRepository videoAnalysisRepository; // VideoAnalysisRepository 주입
    private final UserRepository userRepository;
    /**
     * FastAPI POST 호출 → 응답 JSON을 RDS에 UPSERT.
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

                log.info("FastAPI 처리 성공 → RDS 저장 완료 videoId={} (attempt {}/{})",
                        videoId, attempt, maxAttempts);
                return;

            } catch (Exception e) {
                log.warn("FastAPI 처리 실패 videoId={} (attempt {}/{}): {}",
                        videoId, attempt, maxAttempts, e.toString());
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                backoffMs *= 2;
            }
        }
    }

    @Async
    public void triggerSingleToRdsToUser(String videoId, Long userId) {

        if (videoId == null || videoId.isBlank()) return;

        // videoId만 들어오면 전체 URL로 변환
        String youtubeUrl = videoId.startsWith("http")
                ? videoId
                : "https://www.youtube.com/watch?v=" + videoId;

        // 초기 VideoAnalysis 객체 생성 및 PENDING 상태로 저장
        VideoAnalysis videoAnalysis = VideoAnalysis.builder()
                .videoId(videoId)
                .user(userRepository.findById(userId).orElse(null)) // User 객체는 필요에 따라 로드
                .status(VideoAnalysisStatus.PENDING)
                .build();
        videoAnalysis = videoAnalysisRepository.save(videoAnalysis); // DB에 저장

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
                resultService.upsertFromFastApiResponseToUser(response, userId);

                // 성공 시 status를 COMPLETED로 업데이트
                videoAnalysis = videoAnalysis.toBuilder().status(VideoAnalysisStatus.COMPLETED).build();
                videoAnalysisRepository.save(videoAnalysis);

                log.info("FastAPI 처리 성공 → RDS 저장 완료 videoId={} (attempt {}/{})",
                        videoId, attempt, maxAttempts);
                return;

            } catch (Exception e) {
                log.warn("FastAPI 처리 실패 videoId={} (attempt {}/{}): {}",
                        videoId, attempt, maxAttempts, e.toString());
                // 실패 시 status를 FAILED로 업데이트
                videoAnalysis = videoAnalysis.toBuilder().status(VideoAnalysisStatus.FAILED).build();
                videoAnalysisRepository.save(videoAnalysis);
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                backoffMs *= 2;
            }
        }
    }

    @Async
    public CompletableFuture<VideoAnalysis> triggerSingleToRdsToNotLogin(String videoId) {

        if (videoId == null || videoId.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }

        // videoId만 들어오면 전체 URL로 변환
        String youtubeUrl = videoId.startsWith("http")
                ? videoId
                : "https://www.youtube.com/watch?v=" + videoId;

        // 초기 VideoAnalysis 객체 생성 (DB에 저장하지 않음)
        VideoAnalysis videoAnalysis = VideoAnalysis.builder()
                .videoId(videoId)
                .status(VideoAnalysisStatus.PENDING)
                .build();

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
                videoAnalysis = resultService.upsertFromFastApiNotLogin(response).toBuilder()
                        .status(VideoAnalysisStatus.COMPLETED)
                        .build();
                return CompletableFuture.completedFuture(videoAnalysis);

            } catch (Exception e) {
                log.warn("FastAPI 처리 실패 videoId={} (attempt {}/{}): {}",
                        videoId, attempt, maxAttempts, e.toString());
                videoAnalysis = videoAnalysis.toBuilder().status(VideoAnalysisStatus.FAILED).build();
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                backoffMs *= 2;
            }
        }
        log.error("최대 재시도 횟수(" + maxAttempts + "회)를 초과했습니다. videoId: " + videoId);
        videoAnalysis = videoAnalysis.toBuilder().status(VideoAnalysisStatus.FAILED).build();
        return CompletableFuture.completedFuture(videoAnalysis);
    }
}
