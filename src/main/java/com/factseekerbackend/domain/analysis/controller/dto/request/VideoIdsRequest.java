package com.factseekerbackend.domain.analysis.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record VideoIdsRequest(
        @Schema(description = "조회할 비디오 ID 목록", example = "[\"abc123\", \"def456\"]")
        List<String> videoIds
) {
    public static VideoIdsRequest from(List<String> videoIds) {
        return new VideoIdsRequest(videoIds);
    }
}
