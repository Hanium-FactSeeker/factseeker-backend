package com.factseekerbackend.domain.politician.repository;

import com.factseekerbackend.domain.politician.entity.AnalysisStatus;
import com.factseekerbackend.domain.politician.entity.PoliticianTrustScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PoliticianTrustScoreRepository extends JpaRepository<PoliticianTrustScore, Long> {

    List<PoliticianTrustScore> findByPoliticianIdOrderByAnalysisDateDesc(Long politicianId);

    Optional<PoliticianTrustScore> findByPoliticianIdAndAnalysisDate(Long politicianId, LocalDate analysisDate);

    List<PoliticianTrustScore> findByAnalysisDateOrderByOverallScoreDesc(LocalDate analysisDate);

    List<PoliticianTrustScore> findByAnalysisStatus(AnalysisStatus status);

    @Query("SELECT pts FROM PoliticianTrustScore pts WHERE pts.analysisDate = :analysisDate AND pts.analysisStatus = 'COMPLETED' ORDER BY pts.overallScore DESC")
    List<PoliticianTrustScore> findCompletedScoresByDate(@Param("analysisDate") LocalDate analysisDate);

    @Query("SELECT pts FROM PoliticianTrustScore pts WHERE pts.politician.id = :politicianId AND pts.analysisStatus = 'COMPLETED' ORDER BY pts.analysisDate DESC LIMIT 1")
    Optional<PoliticianTrustScore> findLatestCompletedScore(@Param("politicianId") Long politicianId);

    @Query("SELECT pts FROM PoliticianTrustScore pts WHERE pts.retryCount < 3 AND pts.analysisStatus = 'FAILED'")
    List<PoliticianTrustScore> findFailedScoresForRetry();

    boolean existsByPoliticianIdAndAnalysisDate(Long politicianId, LocalDate analysisDate);
}
