package com.factseekerbackend.domain.analysis.service.fastapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FactCheckRequest(
    @JsonProperty("youtube_url") String youtubeUrl
) {}
