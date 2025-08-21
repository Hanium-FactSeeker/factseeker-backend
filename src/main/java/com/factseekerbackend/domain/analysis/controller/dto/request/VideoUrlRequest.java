package com.factseekerbackend.domain.analysis.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VideoUrlRequest(@JsonProperty("youtube_url") String youtubeUrl) {
}
