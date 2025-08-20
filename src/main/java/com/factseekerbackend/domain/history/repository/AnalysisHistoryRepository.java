package com.factseekerbackend.domain.history.repository;

import com.factseekerbackend.domain.history.entity.AnalysisHistory;
import com.factseekerbackend.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisHistoryRepository extends JpaRepository<AnalysisHistory, Long> {

    Page<AnalysisHistory> findByUser(User user, Pageable pageable);

}
