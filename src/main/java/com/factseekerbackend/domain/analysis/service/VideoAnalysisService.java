package com.factseekerbackend.domain.analysis.service;

import com.factseekerbackend.domain.analysis.controller.dto.response.VideoAnalysisResponse;
import com.factseekerbackend.domain.analysis.entity.VideoAnalysis;
import com.factseekerbackend.domain.analysis.repository.VideoAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VideoAnalysisService {

    private final VideoAnalysisRepository repository;

    public VideoAnalysisResponse getVideoAnalysis(Long userId, Long videoAnalysisId) {
        VideoAnalysis videoAnalysis = repository.findByUserIdAndId(userId, videoAnalysisId).orElseThrow(()->new NullPointerException("해당 비디오를 찾을 수 없습니다."));
        return VideoAnalysisResponse.from(videoAnalysis);
    }
}
