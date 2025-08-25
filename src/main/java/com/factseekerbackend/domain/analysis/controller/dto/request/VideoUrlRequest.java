package com.factseekerbackend.domain.analysis.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VideoUrlRequest(
        @Schema(
                description = "분석할 유튜브 전체 URL (watch/youtu.be/shorts 지원)",
                example = "https://www.youtube.com/watch?v=H-D2LfzB1wM"
        )
        @NotBlank
        @Pattern(
                regexp = "^https://(?:(?:www\\.)?youtube\\.com/watch\\?v=[A-Za-z0-9_-]{11}(?:[&?].*)?|youtu\\.be/[A-Za-z0-9_-]{11}(?:[?&].*)?|(?:www\\.)?youtube\\.com/shorts/[A-Za-z0-9_-]{11}(?:[?&].*)?)$"
        )
        @JsonProperty("youtube_url") String youtubeUrl
) {
}
