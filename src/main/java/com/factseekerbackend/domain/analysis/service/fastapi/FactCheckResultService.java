package com.factseekerbackend.domain.analysis.service.fastapi;

import com.factseekerbackend.domain.analysis.controller.dto.response.VideoAnalysisResponse;
import com.factseekerbackend.domain.analysis.controller.dto.response.FastApiFactCheckResponse;
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

            Top10VideoAnalysis top10VideoAnalysis = top10VideoAnalysisRepository.findById(dto.videoId())
                    .orElseGet(() -> Top10VideoAnalysis.builder().videoId(dto.videoId()).build())
                    .toBuilder()
                    .totalConfidenceScore(dto.totalConfidenceScore())
                    .summary(dto.summary())
                    .channelType(dto.channelType())
                    .channelTypeReason(dto.channelTypeReason())
                    .claims(dto.channelType())
                    .createdAt(dto.createdAt())
                    .build();

            top10VideoAnalysisRepository.save(top10VideoAnalysis);
        } catch (Exception ignore) {
        }
    }

    public VideoAnalysisResponse buildResponseFromFastApiNotLogin(String json) {
        FastApiFactCheckResponse dto = parse(json);
        if (dto == null || dto.videoId() == null || dto.videoId().isBlank()) return null;

        return VideoAnalysisResponse.builder()
                .videoId(dto.videoId())
                .totalConfidenceScore(dto.totalConfidenceScore())
                .summary(dto.summary())
                .channelType(dto.channelType())
                .channelTypeReason(dto.channelTypeReason())
                .claims(dto.claims())
                .createdAt(dto.createdAt())
                .status(VideoAnalysisStatus.COMPLETED)
                .build();
    }

    public void upsertFromFastApiResponseToUser(String json, Long userId) {
        try {
            FastApiFactCheckResponse dto = parse(json);
            if (dto == null || dto.videoId() == null || dto.videoId().isBlank()) return;

            VideoAnalysis videoAnalysis = VideoAnalysis.builder()
                    .videoId(dto.videoId())
                    .totalConfidenceScore(dto.totalConfidenceScore())
                    .summary(dto.summary())
                    .channelType(dto.channelType())
                    .channelTypeReason(dto.channelTypeReason())
                    .claims(dto.claims())
                    .user(userRepository.getReferenceById(userId))
                    .createdAt(dto.createdAt())
                    .build();

            videoAnalysisRepository.save(videoAnalysis);
        } catch (Exception ignore) {
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
