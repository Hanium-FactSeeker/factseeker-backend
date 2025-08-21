package com.factseekerbackend.domain.analysis.service;

import com.factseekerbackend.domain.analysis.controller.dto.response.ClaimDto;
import com.factseekerbackend.domain.analysis.controller.dto.response.VideoAnalysisResponse;
import com.factseekerbackend.domain.analysis.entity.VideoAnalysis;
import com.factseekerbackend.domain.analysis.repository.VideoAnalysisRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class VideoAnalysisService {

    private final VideoAnalysisRepository repository;
    private final ObjectMapper om;

    public VideoAnalysisResponse getVideoAnalysis(Long userId, Long videoAnalysisId) {
        VideoAnalysis videoAnalysis = repository.findByUserIdAndId(userId, videoAnalysisId)
                .orElseThrow(() -> new NullPointerException("해당 비디오를 찾을 수 없습니다."));

        Object claims = parseClaims(videoAnalysis.getClaims());

        return VideoAnalysisResponse.from(videoAnalysis, claims);
    }

    private Object parseClaims(String claimsJson) {
        if (claimsJson == null || claimsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return om.readValue(claimsJson, new TypeReference<List<ClaimDto>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse claims JSON: {}", claimsJson, e);
            // 파싱 실패 시, 원본 문자열을 그대로 반환하거나 비어있는 리스트를 반환할 수 있습니다.
            // 여기서는 비어있는 리스트를 반환하여 프론트엔드의 일관성을 유지합니다.
            return Collections.emptyList();
        }
    }
}
