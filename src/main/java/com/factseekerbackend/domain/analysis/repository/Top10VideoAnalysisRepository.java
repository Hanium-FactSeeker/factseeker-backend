package com.factseekerbackend.domain.analysis.repository;


import com.factseekerbackend.domain.analysis.entity.Top10VideoAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Top10VideoAnalysisRepository extends JpaRepository<Top10VideoAnalysis, String> {
}
