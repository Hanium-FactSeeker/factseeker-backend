package com.factseekerbackend.domain.analysis.controller;

import com.factseekerbackend.domain.analysis.controller.dto.request.VideoUrlDto;
import com.factseekerbackend.domain.analysis.controller.dto.response.VideoAnalysisResponse;
import com.factseekerbackend.domain.analysis.service.VideoAnalysisService;
import com.factseekerbackend.domain.analysis.service.fastapi.FactCheckTriggerService;
import com.factseekerbackend.domain.user.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor()
@RequestMapping
public class VideoAnalysisController {

    private final FactCheckTriggerService factCheckTriggerService;
    private final VideoAnalysisService videoAnalysisService;

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
            @RequestBody VideoUrlDto videoUrlDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String youtubeUrl = videoUrlDto.youtubeUrl();
        if (userDetails != null){
            Long userId = userDetails.getId();
            factCheckTriggerService.triggerSingleToRdsToUser(youtubeUrl, userId);
            return CompletableFuture.completedFuture(ResponseEntity.ok("저장 성공"));
        } else {
            return factCheckTriggerService.triggerSingleToRdsToNotLogin(youtubeUrl)
                    .thenApply(ResponseEntity::ok);
        }
    }

}
