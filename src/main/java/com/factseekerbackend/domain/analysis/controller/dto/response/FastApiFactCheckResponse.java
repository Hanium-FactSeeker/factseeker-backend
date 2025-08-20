package com.factseekerbackend.domain.analysis.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record FastApiFactCheckResponse(
        @JsonProperty("video_id") String videoId,
        @JsonProperty("video_total_confidence_score") Integer totalConfidenceScore,
        @JsonProperty("summary") String summary,
        @JsonProperty("channel_type") String channelType,
        @JsonProperty("channel_type_reason") String channelTypeReason,
        @JsonProperty("claims") String claims,
        @JsonProperty("created_at") LocalDateTime createdAt
) {}

