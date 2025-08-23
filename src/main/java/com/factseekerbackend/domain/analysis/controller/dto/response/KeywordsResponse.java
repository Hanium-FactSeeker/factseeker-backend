package com.factseekerbackend.domain.analysis.controller.dto.response;

import com.factseekerbackend.domain.analysis.entity.Top10VideoAnalysis;

import java.util.Arrays;
import java.util.List;

public record KeywordsResponse(List<String> keywords) {
    public static KeywordsResponse from(Top10VideoAnalysis top10VideoAnalysis) {
        List<String> list = Arrays.stream(top10VideoAnalysis.getKeywords().trim().split(","))
                .map(String::trim)
                .toList();
        return new KeywordsResponse(list);
    }
}
