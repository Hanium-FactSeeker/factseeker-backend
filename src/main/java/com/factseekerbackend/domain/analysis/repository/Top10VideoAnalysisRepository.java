package com.factseekerbackend.domain.analysis.repository;


import com.factseekerbackend.domain.analysis.entity.video.Top10VideoAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Top10VideoAnalysisRepository extends JpaRepository<Top10VideoAnalysis, String> {
    List<Top10VideoAnalysis> findByVideoId(String videoId);
}
