package com.factseekerbackend.domain.youtube.service.fastapi;

import com.factseekerbackend.domain.youtube.entity.Top10FactCheckResult;
import com.factseekerbackend.domain.youtube.repository.FactCheckResultRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FactCheckResultService {

    private final FactCheckResultRepository repo;
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

            Top10FactCheckResult e = repo.findById(videoId)
                    .orElseGet(() -> Top10FactCheckResult.builder().videoId(videoId).build())
                    .toBuilder()
                    .totalConfidenceScore(score)
                    .summary(summary)
                    .channelType(channelType)
                    .channelTypeReason(reason)
                    .resultJson(json)
                    .build();

            repo.save(e);
        } catch (Exception ignore) {}
    }

    private String getText(JsonNode n, String f) { return n.has(f) && !n.get(f).isNull() ? n.get(f).asText() : null; }
    private Integer getInt(JsonNode n, String f) { return n.has(f) && n.get(f).canConvertToInt() ? n.get(f).asInt() : null; }
}