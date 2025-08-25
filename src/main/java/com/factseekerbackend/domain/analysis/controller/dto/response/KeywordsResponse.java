package com.factseekerbackend.domain.analysis.controller.dto.response;

import com.factseekerbackend.domain.analysis.entity.video.Top10VideoAnalysis;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;
import java.util.List;

public record KeywordsResponse(
        @Schema(description = "키워드 배열", example = "[\"정책\", \"경제\", \"토론\"]")
        List<String> keywords
) {
    public static KeywordsResponse from(Top10VideoAnalysis top10VideoAnalysis) {
        String keywords = top10VideoAnalysis.getKeywords();
        if (keywords == null || keywords.isBlank()) {
            return new KeywordsResponse(List.of());
        }
        List<String> list = Arrays.stream(keywords.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        return new KeywordsResponse(list);
    }
}
