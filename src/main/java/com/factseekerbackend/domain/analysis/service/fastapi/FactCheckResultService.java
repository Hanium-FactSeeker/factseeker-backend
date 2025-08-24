package com.factseekerbackend.domain.analysis.service.fastapi;

import com.factseekerbackend.domain.analysis.controller.dto.response.fastapi.FastApiFactCheckResponse;
import com.factseekerbackend.domain.analysis.controller.dto.response.VideoAnalysisResponse;
import com.factseekerbackend.domain.analysis.entity.Top10VideoAnalysis;
import com.factseekerbackend.domain.analysis.entity.VideoAnalysis;
import com.factseekerbackend.domain.analysis.entity.VideoAnalysisStatus;
import com.factseekerbackend.domain.analysis.repository.Top10VideoAnalysisRepository;
import com.factseekerbackend.domain.analysis.repository.VideoAnalysisRepository;
import com.factseekerbackend.domain.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FactCheckResultService {

    private final Top10VideoAnalysisRepository top10VideoAnalysisRepository;
    private final VideoAnalysisRepository videoAnalysisRepository;
    private final UserRepository userRepository;
    private final ObjectMapper om;

    public void upsertFromFastApiResponse(String json) {
        try {
            FastApiFactCheckResponse dto = parse(json);
            if (dto == null || dto.videoId() == null || dto.videoId().isBlank()) return;

            String claimsAsJson = om.writeValueAsString(dto.claims());

            Top10VideoAnalysis top10VideoAnalysis = top10VideoAnalysisRepository.findById(dto.videoId())
                    .orElseGet(() -> Top10VideoAnalysis.builder().videoId(dto.videoId()).build())
                    .toBuilder()
                    .videoUrl(dto.videoUrl())
                    .totalConfidenceScore(dto.videoTotalConfidenceScore())
                    .summary(dto.summary())
                    .channelType(dto.channelType())
                    .channelTypeReason(dto.channelTypeReason())
                    .claims(claimsAsJson)
                    .keywords(dto.keywords())
                    .threeLineSummary(dto.threeLineSummary())
                    .createdAt(dto.createdAt())
                    .build();

            top10VideoAnalysisRepository.save(top10VideoAnalysis);
        } catch (Exception e) {
            log.error("Error in upsertFromFastApiResponse: {}", e.getMessage(), e);
        }
    }

    public VideoAnalysisResponse buildResponseFromFastApiNotLogin(String json) {
        FastApiFactCheckResponse dto = parse(json);
        if (dto == null || dto.videoId() == null || dto.videoId().isBlank()) return null;

        return VideoAnalysisResponse.builder()
                .videoId(dto.videoId())
                .videoUrl(dto.videoUrl())
                .totalConfidenceScore(dto.videoTotalConfidenceScore())
                .summary(dto.summary())
                .channelType(dto.channelType())
                .channelTypeReason(dto.channelTypeReason())
                .claims(dto.claims()) // Pass the list of ClaimDto directly
                .keywords(dto.keywords())
                .threeLineSummary(dto.threeLineSummary())
                .createdAt(dto.createdAt())
                .status(VideoAnalysisStatus.COMPLETED)
                .build();
    }

    public void upsertFromFastApiResponseToUser(String json, Long userId) {
        try {
            FastApiFactCheckResponse dto = parse(json);
            if (dto == null || dto.videoId() == null || dto.videoId().isBlank()) return;

            String claimsAsJson = om.writeValueAsString(dto.claims());

            VideoAnalysis videoAnalysis = VideoAnalysis.builder()
                    .videoId(dto.videoId())
                    .videoUrl(dto.videoUrl())
                    .totalConfidenceScore(dto.videoTotalConfidenceScore())
                    .summary(dto.summary())
                    .channelType(dto.channelType())
                    .channelTypeReason(dto.channelTypeReason())
                    .claims(claimsAsJson)
                    .keywords(dto.keywords())
                    .threeLineSummary(dto.threeLineSummary())
                    .user(userRepository.getReferenceById(userId))
                    .createdAt(dto.createdAt())
                    .status(VideoAnalysisStatus.COMPLETED)
                    .build();

            videoAnalysisRepository.save(videoAnalysis);
        } catch (Exception e) {
            log.error("Error in upsertFromFastApiResponseToUser: {}", e.getMessage(), e);
        }
    }

    /**
     * 기존 VideoAnalysis 레코드(ID 기반)를 FastAPI 응답 내용으로 업데이트한다.
     */
    public void updateExistingFromFastApiResponseToUser(String json, Long videoAnalysisId) {
        try {
            FastApiFactCheckResponse dto = parse(json);
            if (dto == null || dto.videoId() == null || dto.videoId().isBlank()) return;

            String claimsAsJson = om.writeValueAsString(dto.claims());

            videoAnalysisRepository.findById(videoAnalysisId).ifPresent(existing -> {
                VideoAnalysis updated = existing.toBuilder()
                        .videoId(dto.videoId())
                        .videoUrl(dto.videoUrl())
                        .totalConfidenceScore(dto.videoTotalConfidenceScore())
                        .summary(dto.summary())
                        .channelType(dto.channelType())
                        .channelTypeReason(dto.channelTypeReason())
                        .claims(claimsAsJson)
                        .keywords(dto.keywords())
                        .threeLineSummary(dto.threeLineSummary())
                        .createdAt(dto.createdAt())
                        .status(VideoAnalysisStatus.COMPLETED)
                        .build();
                videoAnalysisRepository.save(updated);
            });
        } catch (Exception e) {
            log.error("Error in updateExistingFromFastApiResponseToUser: {}", e.getMessage(), e);
        }
    }

    private FastApiFactCheckResponse parse(String json) {
        try {
            return om.readValue(json, FastApiFactCheckResponse.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse FastAPI response: {}", e.getMessage());
            return null;
        }
    }
}
