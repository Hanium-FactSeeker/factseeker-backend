package com.factseekerbackend.domain.youtube.repository;

import com.factseekerbackend.domain.youtube.entity.Top10FactCheckResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactCheckResultRepository extends JpaRepository<Top10FactCheckResult, String> {
}
