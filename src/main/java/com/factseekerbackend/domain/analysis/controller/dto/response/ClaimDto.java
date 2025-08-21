package com.factseekerbackend.domain.analysis.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ClaimDto(
    @JsonProperty("claim") String claim,
    @JsonProperty("result") String result,
    @JsonProperty("confidence_score") Integer confidenceScore,
    @JsonProperty("evidence") List<EvidenceDto> evidence
) {}
