package com.factseekerbackend.domain.analysis.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EvidenceDto(
    @JsonProperty("url") String url,
    @JsonProperty("relevance") String relevance,
    @JsonProperty("fact_check_result") String factCheckResult,
    @JsonProperty("justification") String justification,
    @JsonProperty("snippet") String snippet
) {}
