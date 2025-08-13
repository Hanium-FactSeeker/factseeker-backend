package com.factseekerbackend.domain.analysis.service.fastapi;

import com.factseekerbackend.domain.analysis.entity.Top10VideoAnalysis;
import com.factseekerbackend.domain.analysis.entity.VideoAnalysis;
import com.factseekerbackend.domain.analysis.repository.Top10VideoAnalysisRepository;
import com.factseekerbackend.domain.analysis.repository.VideoAnalysisRepository;
import com.factseekerbackend.domain.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FactCheckResultService {

    private final Top10VideoAnalysisRepository top10VideoAnalysisRepository;
    private final VideoAnalysisRepository videoAnalysisRepository;
    private final UserRepository userRepository;
    private final ObjectMapper om = new ObjectMapper();

    public void upsertFromFastApiResponse(String json) {
        try {
            JsonNode root = om.readTree(json);

            String videoId = getText(root, "video_id");
            if (videoId == null || videoId.isBlank()) return;

            Integer score = getInt(root, "video_total_confidence_score");
            String summary = getText(root, "summary");
            String channelType = getText(root, "channel_type");
            String reason = getText(root, "channel_type_reason");
            LocalDateTime createdAt = getLocalDateTime(root, "created_at");

            Top10VideoAnalysis top10VideoAnalysis = top10VideoAnalysisRepository.findById(videoId)
                    .orElseGet(() -> Top10VideoAnalysis.builder().videoId(videoId).build())
                    .toBuilder()
                    .totalConfidenceScore(score)
                    .summary(summary)
                    .channelType(channelType)
                    .channelTypeReason(reason)
                    .resultJson(json)
                    .createdAt(createdAt)
                    .build();

            top10VideoAnalysisRepository.save(top10VideoAnalysis);
        } catch (Exception ignore) {

        }
    }

    public VideoAnalysis upsertFromFastApiNotLogin(String json) {

        JsonNode root = null;
        try {
            root = om.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String videoId = getText(root, "video_id");
            if (videoId == null || videoId.isBlank()) {
                return null;
            }

            Integer score = getInt(root, "video_total_confidence_score");
            String summary = getText(root, "summary");
            String channelType = getText(root, "channel_type");
            String reason = getText(root, "channel_type_reason");
            LocalDateTime createdAt = getLocalDateTime(root, "created_at");

            return VideoAnalysis.builder()
                    .videoId(videoId)
                    .totalConfidenceScore(score)
                    .summary(summary)
                    .channelType(channelType)
                    .channelTypeReason(reason)
                    .resultJson(json)
                    .createdAt(createdAt)
                    .build();


    }

    public void upsertFromFastApiResponseToUser(String json, Long userId) {
        try {
            JsonNode root = om.readTree(json);

            String videoId = getText(root, "video_id");
            if (videoId == null || videoId.isBlank()) return;

            Integer score = getInt(root, "video_total_confidence_score");
            String summary = getText(root, "summary");
            String channelType = getText(root, "channel_type");
            String reason = getText(root, "channel_type_reason");
            LocalDateTime createdAt = getLocalDateTime(root, "created_at");

            VideoAnalysis videoAnalysis = VideoAnalysis.builder()
                    .videoId(videoId)
                    .totalConfidenceScore(score)
                    .summary(summary)
                    .channelType(channelType)
                    .channelTypeReason(reason)
                    .resultJson(json)
                    .user(
                            userRepository.getReferenceById(userId)
                    )
                    .createdAt(createdAt)
                    .build();

            videoAnalysisRepository.save(videoAnalysis);
        } catch (Exception ignore) {

        }
    }







    private String getText(JsonNode n, String f) { return n.has(f) && !n.get(f).isNull() ? n.get(f).asText() : null; }
    private Integer getInt(JsonNode n, String f) { return n.has(f) && n.get(f).canConvertToInt() ? n.get(f).asInt() : null; }

    private LocalDateTime getLocalDateTime(JsonNode n, String f) {
        if (n.has(f) && !n.get(f).isNull()) {
            try {
                return LocalDateTime.parse(n.get(f).asText(), DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                log.warn("Failed to parse LocalDateTime from JSON field {}: {}", f, n.get(f).asText(), e);
            }
        }
        return null;
    }
}
