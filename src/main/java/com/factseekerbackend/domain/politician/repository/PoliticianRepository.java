package com.factseekerbackend.domain.politician.repository;

import com.factseekerbackend.domain.politician.entity.Politician;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PoliticianRepository extends JpaRepository<Politician, Long> {

    // 정확 일치 여러 건 중 "gpt_trust_score DESC, id ASC"로 1건만 선택
    Optional<Politician> findFirstByNameKrOrderByGptTrustScoreDescIdAsc(String nameKr);

    // 백업: 부분 일치 중 첫 1건
    Optional<Politician> findFirstByNameKrContainingIgnoreCase(String nameKr);

    // 상위 12명 (gpt_trust_score 기준, NULL은 0으로)
    @Query("""
           SELECT p FROM Politician p
           ORDER BY COALESCE(p.gptTrustScore, 0) DESC, p.id ASC
           """)
    List<Politician> findTopByGptScore(Pageable pageable);
}