package com.factseekerbackend.domain.politician.repository;

import com.factseekerbackend.domain.politician.entity.Politician;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PoliticianRepository extends JpaRepository<Politician, Long> {

    List<Politician> findByIsActiveTrue();

    Optional<Politician> findByNameAndIsActiveTrue(String name);

    @Query("SELECT p FROM Politician p WHERE p.isActive = true ORDER BY p.name")
    List<Politician> findAllActiveOrderByName();

    boolean existsByNameAndIsActiveTrue(String name);

    // 정확 일치 여러 건 중 첫 번째 선택 (id 기준)
    Optional<Politician> findFirstByNameOrderByIdAsc(String name);

    // 백업: 부분 일치 중 첫 1건
    Optional<Politician> findFirstByNameContainingIgnoreCase(String name);


}
