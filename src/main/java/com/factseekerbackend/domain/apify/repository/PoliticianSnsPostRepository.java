package com.factseekerbackend.domain.apify.repository;

import com.factseekerbackend.domain.apify.entity.PoliticianSnsPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoliticianSnsPostRepository extends JpaRepository<PoliticianSnsPost, Long> {
}
