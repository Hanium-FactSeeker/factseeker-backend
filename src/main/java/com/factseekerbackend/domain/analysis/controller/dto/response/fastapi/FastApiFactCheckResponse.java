package com.factseekerbackend.domain.analysis.controller.dto.response.fastapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public record FastApiFactCheckResponse(
    @JsonProperty("video_id") String videoId,
    @JsonProperty("video_url") String videoUrl,
    @JsonProperty("video_total_confidence_score") Integer videoTotalConfidenceScore,
    @JsonProperty("claims") List<ClaimDto> claims,
    @JsonProperty("summary") String summary,
    @JsonProperty("channel_type") String channelType,
    @JsonProperty("channel_type_reason") String channelTypeReason,
    @JsonProperty("created_at") LocalDateTime createdAt,
    @JsonProperty("keywords") String keywords,
    @JsonProperty("three_line_summary") String threeLineSummary
) {}

