package com.factseekerbackend.domain.politician.repository;

import com.factseekerbackend.domain.politician.entity.Politician;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoliticianRepository extends JpaRepository<Politician, Long> {

    // 정확 일치 여러 건 중 첫 번째 선택 (id 기준)
    Optional<Politician> findFirstByNameOrderByIdAsc(String name);

    // 백업: 부분 일치 전체
    List<Politician> findByNameContainingIgnoreCase(String name);

    // 추가: SNS username이 포함된 X URL로 politician 조회
    Optional<Politician> findByXUrlContaining(String username);
}
