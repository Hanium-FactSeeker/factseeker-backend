package com.factseekerbackend.domain.analysis.controller;

import com.factseekerbackend.domain.analysis.controller.dto.request.VideoUrlRequest;
import com.factseekerbackend.domain.analysis.controller.dto.response.VideoAnalysisResponse;
import com.factseekerbackend.domain.analysis.entity.VideoAnalysisStatus;
import com.factseekerbackend.domain.analysis.service.VideoAnalysisService;
import com.factseekerbackend.domain.analysis.service.fastapi.FactCheckTriggerService;
import com.factseekerbackend.global.auth.jwt.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;
import com.factseekerbackend.domain.analysis.entity.Top10VideoAnalysis;
import com.factseekerbackend.domain.analysis.repository.Top10VideoAnalysisRepository;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequiredArgsConstructor()
@RequestMapping
public class VideoAnalysisController {

    private final FactCheckTriggerService factCheckTriggerService;
    private final VideoAnalysisService videoAnalysisService;
    private final Top10VideoAnalysisRepository top10VideoAnalysisRepository;

    @GetMapping("/analysis/{videoAnalysisId}")
    public ResponseEntity<VideoAnalysisResponse> getVideoAnalysis(
            @PathVariable("videoAnalysisId") Long videoAnalysisId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        VideoAnalysisResponse videoAnalysis = videoAnalysisService.getVideoAnalysis(userId, videoAnalysisId);

        return ResponseEntity.ok(videoAnalysis);
    }

    @PostMapping("/analysis")
    public CompletableFuture<ResponseEntity<?>> saveVideoAnalysis(
            @RequestBody VideoUrlRequest videoUrlRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String youtubeUrl = videoUrlRequest.youtubeUrl();
        if (userDetails != null){
            Long userId = userDetails.getId();
            factCheckTriggerService.triggerSingleToRdsToUser(youtubeUrl, userId);
            return CompletableFuture.completedFuture(ResponseEntity.ok("저장 성공"));
        } else {
            return factCheckTriggerService.triggerSingleToRdsToNotLogin(youtubeUrl)
                    .thenApply(ResponseEntity::ok);
        }
    }

    @GetMapping("/analysis/top10/{videoId}")
    public ResponseEntity<VideoAnalysisResponse> getTop10VideoAnalysis(
            @PathVariable("videoId") String videoId) {
        return top10VideoAnalysisRepository.findById(videoId)
                .map(analysis -> ResponseEntity.ok(VideoAnalysisResponse.from(analysis)))
                .orElseGet(() -> ResponseEntity.accepted().body(VideoAnalysisResponse.builder()
                        .videoId(videoId)
                        .status(VideoAnalysisStatus.PENDING)
                        .build()));
    }

}
