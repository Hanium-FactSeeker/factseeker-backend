package com.factseekerbackend.domain.analysis.repository;

import com.factseekerbackend.domain.analysis.entity.VideoAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoAnalysisRepository extends JpaRepository<VideoAnalysis, Long> {
    Optional<VideoAnalysis> findByUserIdAndId(Long userId, Long videoAnalysisId);
}
