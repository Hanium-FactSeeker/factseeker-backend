package com.factseekerbackend.domain.analysis.service.fastapi;

import com.factseekerbackend.domain.analysis.controller.dto.response.VideoAnalysisResponse;
import com.factseekerbackend.domain.analysis.entity.VideoAnalysis;
import com.factseekerbackend.domain.analysis.repository.VideoAnalysisRepository;
import com.factseekerbackend.domain.analysis.service.fastapi.dto.FactCheckRequest;
import com.factseekerbackend.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper om;

    /**
     * FastAPI POST 호출 → 응답 JSON을 RDS에 UPSERT.
     */
    public void triggerSingleToRds(String videoId) {

        if (videoId == null || videoId.isBlank()) return;

        // videoId만 들어오면 전체 URL로 변환
        String youtubeUrl = videoId.startsWith("https")
                ? videoId
                : "https://www.youtube.com/watch?v=" + videoId;

        int maxAttempts = 3;
        long backoffMs = 200L;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                String requestJson = om.writeValueAsString(new FactCheckRequest(youtubeUrl));
                String response = fastApiClient.post()
                        .uri("/fact-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", videoId)
                        .header("X-Requested-By", "spring-cron")
                        .body(requestJson)
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
                
                if (attempt == maxAttempts) {
                    log.error("최종 실패: FastAPI 처리 실패 videoId={}. FAILED 상태로 저장합니다.", videoId);
                    resultService.saveFailedTop10Analysis(videoId);
                }

                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                backoffMs *= 2;
            }
        }
    }

    /**
     * 로그인 사용자: 즉시 PENDING 레코드를 만들고 ID를 반환한 뒤,
     * 비동기로 FastAPI 호출 및 결과 업데이트 진행.
     */
    public Long triggerAndReturnId(String videoId, Long userId) {
        if (videoId == null || videoId.isBlank()) return null;

        String youtubeUrl = videoId.startsWith("http")
                ? videoId
                : "https://www.youtube.com/watch?v=" + videoId;

        String normalizedVideoId = extractYoutubeVideoId(videoId);
        if (normalizedVideoId == null || normalizedVideoId.isBlank()) {
            normalizedVideoId = videoId;
        }

        VideoAnalysis pending = VideoAnalysis.builder()
                .videoId(normalizedVideoId)
                .videoUrl(youtubeUrl)
                .user(userRepository.findById(userId).orElse(null))
                .status(VideoAnalysisStatus.PENDING)
                .build();
        pending = videoAnalysisRepository.save(pending);

        // 비동기 처리 시작
        processFactCheck(pending.getId(), normalizedVideoId, youtubeUrl, userId);

        return pending.getId();
    }

    @Async("factCheckExecutor")
    public void processFactCheck(Long videoAnalysisId, String normalizedVideoId, String youtubeUrl, Long userId) {
        int maxAttempts = 3;
        long backoffMs = 200L;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                String requestJson = om.writeValueAsString(new FactCheckRequest(youtubeUrl));
                String response = fastApiClient.post()
                        .uri("/fact-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", normalizedVideoId)
                        .header("X-Requested-By", "spring-cron")
                        .body(requestJson)
                        .retrieve()
                        .body(String.class);

                // 기존 PENDING 레코드를 FastAPI 결과로 업데이트
                resultService.updateExistingFromFastApiResponseToUser(response, videoAnalysisId);

                log.info("FastAPI 처리 성공 → RDS 업데이트 완료 videoId={} (attempt {}/{})",
                        normalizedVideoId, attempt, maxAttempts);
                return;

            } catch (Exception e) {
                log.warn("FastAPI 처리 실패 videoId={} (attempt {}/{}): {}",
                        normalizedVideoId, attempt, maxAttempts, e.toString());
                // 실패 시 해당 레코드 상태를 FAILED로 업데이트
                try {
                    videoAnalysisRepository.findById(videoAnalysisId).ifPresent(va -> {
                        VideoAnalysis failed = va.toBuilder().status(VideoAnalysisStatus.FAILED).build();
                        videoAnalysisRepository.save(failed);
                    });
                } catch (Exception ignored) {}

                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                backoffMs *= 2;
            }
        }
    }

    @Async("factCheckExecutor")
    public CompletableFuture<VideoAnalysisResponse> triggerSingleToRdsToNotLogin(String videoId) {

        if (videoId == null || videoId.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }

        // videoId만 들어오면 전체 URL로 변환
        String youtubeUrl = videoId.startsWith("http")
                ? videoId
                : "https://www.youtube.com/watch?v=" + videoId;

        // 초기 응답 상태만 표현
        VideoAnalysisResponse pending = VideoAnalysisResponse.builder()
                .videoId(videoId)
                .status(VideoAnalysisStatus.PENDING)
                .build();

        int maxAttempts = 3;
        long backoffMs = 200L;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                String requestJson = om.writeValueAsString(new FactCheckRequest(youtubeUrl));
                String response = fastApiClient.post()
                        .uri("/fact-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", videoId)
                        .header("X-Requested-By", "spring-cron")
                        .body(requestJson)
                        .retrieve()
                        .body(String.class);

                VideoAnalysisResponse dto = resultService.buildResponseFromFastApiNotLogin(response);
                return CompletableFuture.completedFuture(dto);

            } catch (Exception e) {
                log.warn("FastAPI 처리 실패 videoId={} (attempt {}/{}): {}",
                        videoId, attempt, maxAttempts, e.toString());
                pending = VideoAnalysisResponse.builder()
                        .videoId(videoId)
                        .status(VideoAnalysisStatus.FAILED)
                        .build();
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                backoffMs *= 2;
            }
        }
        log.error("최대 재시도 횟수(" + maxAttempts + "회)를 초과했습니다. videoId: " + videoId);
        pending = VideoAnalysisResponse.builder()
                .videoId(videoId)
                .status(VideoAnalysisStatus.FAILED)
                .build();
        return CompletableFuture.completedFuture(pending);
    }

    // 다양한 YouTube URL 형태에서 videoId를 추출
    private String extractYoutubeVideoId(String input) {
        if (input == null || input.isBlank()) return null;
        String s = input.trim();
        try {
            // 이미 순수 ID처럼 보이면 그대로 반환 (YouTube 기본 11자, 더 긴 경우도 호환)
            if (!s.startsWith("http")) {
                return s;
            }

            // 표준 watch URL: https://www.youtube.com/watch?v=VIDEO_ID
            int vIdx = s.indexOf("v=");
            if (vIdx != -1) {
                String candidate = s.substring(vIdx + 2);
                int amp = candidate.indexOf('&');
                if (amp != -1) candidate = candidate.substring(0, amp);
                return candidate;
            }

            // 짧은 URL: https://youtu.be/VIDEO_ID
            String youtu = "youtu.be/";
            int shortIdx = s.indexOf(youtu);
            if (shortIdx != -1) {
                String candidate = s.substring(shortIdx + youtu.length());
                int q = candidate.indexOf('?');
                if (q != -1) candidate = candidate.substring(0, q);
                return candidate;
            }

            // Shorts: https://www.youtube.com/shorts/VIDEO_ID
            String shorts = "/shorts/";
            int shortsIdx = s.indexOf(shorts);
            if (shortsIdx != -1) {
                String candidate = s.substring(shortsIdx + shorts.length());
                int q = candidate.indexOf('?');
                if (q != -1) candidate = candidate.substring(0, q);
                return candidate;
            }
        } catch (Exception ignored) {
        }
        return s; // 파싱 실패 시 원본 반환
    }
}
