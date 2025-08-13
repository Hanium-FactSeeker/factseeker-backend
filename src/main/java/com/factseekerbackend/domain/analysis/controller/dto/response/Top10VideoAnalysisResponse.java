package com.factseekerbackend.domain.analysis.controller.dto.response;

import com.factseekerbackend.domain.analysis.entity.Top10VideoAnalysis;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public record Top10VideoAnalysisResponse(
        String videoId,
        Integer totalConfidenceScore,
        String summary,
        String channelType,
        String channelTypeReason,
        JsonNode resultJson
) {
    public static Top10VideoAnalysisResponse from(Top10VideoAnalysis entity) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(entity.getResultJson());
        } catch (Exception e) {
            // Handle exception, e.g., log it
        }

        return new Top10VideoAnalysisResponse(
                entity.getVideoId(),
                entity.getTotalConfidenceScore(),
                entity.getSummary(),
                entity.getChannelType(),
                entity.getChannelTypeReason(),
                jsonNode
        );
    }
}