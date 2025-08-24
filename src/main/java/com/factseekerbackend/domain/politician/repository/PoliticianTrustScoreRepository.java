package com.factseekerbackend.domain.politician.repository;

import com.factseekerbackend.domain.politician.entity.PoliticianTrustScore;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PoliticianTrustScoreRepository extends JpaRepository<PoliticianTrustScore, Long> {

  Optional<PoliticianTrustScore> findByPoliticianIdAndAnalysisDate(Long politicianId, LocalDate analysisDate);

  @Query("SELECT pts FROM PoliticianTrustScore pts WHERE pts.politician.id = :politicianId AND pts.analysisStatus = 'COMPLETED' ORDER BY pts.analysisDate DESC LIMIT 1")
  Optional<PoliticianTrustScore> findLatestCompletedScore(@Param("politicianId") Long politicianId);

  @Query("SELECT pts FROM PoliticianTrustScore pts WHERE pts.retryCount < 3 AND pts.analysisStatus = 'FAILED'")
  List<PoliticianTrustScore> findFailedScoresForRetry();

  // 상위 12명을 overallScore 기준으로 조회 (최신 분석 결과)
  @Query("SELECT pts FROM PoliticianTrustScore pts " +
      "WHERE pts.analysisStatus = 'COMPLETED' " +
      "AND pts.overallScore IS NOT NULL " +
      "AND pts.analysisDate = (" +
      "  SELECT MAX(pts2.analysisDate) FROM PoliticianTrustScore pts2 " +
      "  WHERE pts2.politician.id = pts.politician.id " +
      "  AND pts2.analysisStatus = 'COMPLETED'" +
      ") " +
      "ORDER BY pts.overallScore DESC")
  Page<PoliticianTrustScore> findTop12ByOverallScoreOrderByDateDesc(Pageable pageable);

  // 모든 정치인의 최신 신뢰도 점수를 최신순(수정일)으로 정렬하여 조회
  @Query("SELECT pts FROM PoliticianTrustScore pts " +
      "WHERE pts.analysisStatus = 'COMPLETED' " +
      "AND pts.analysisDate = (" +
      "  SELECT MAX(pts2.analysisDate) FROM PoliticianTrustScore pts2 " +
      "  WHERE pts2.politician.id = pts.politician.id " +
      "  AND pts2.analysisStatus = 'COMPLETED'" +
      ") " +
      "ORDER BY pts.lastUpdated DESC")
  Page<PoliticianTrustScore> findLatestCompletedScoresOrderByLatest(Pageable pageable);

  // 모든 정치인의 최신 신뢰도 점수를 신뢰도순으로 정렬하여 조회
  @Query("SELECT pts FROM PoliticianTrustScore pts " +
      "WHERE pts.analysisStatus = 'COMPLETED' " +
      "AND pts.analysisDate = (" +
      "  SELECT MAX(pts2.analysisDate) FROM PoliticianTrustScore pts2 " +
      "  WHERE pts2.politician.id = pts.politician.id " +
      "  AND pts2.analysisStatus = 'COMPLETED'" +
      ") " +
      "ORDER BY pts.overallScore DESC")
  Page<PoliticianTrustScore> findLatestCompletedScoresOrderByTrustScore(Pageable pageable);
}
