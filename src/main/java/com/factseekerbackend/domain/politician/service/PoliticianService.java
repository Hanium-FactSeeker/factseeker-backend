package com.factseekerbackend.domain.politician.service;

import com.factseekerbackend.domain.politician.dto.response.PoliticianResponse;
import com.factseekerbackend.domain.politician.entity.Politician;
import com.factseekerbackend.domain.politician.entity.PoliticianTrustScore;
import com.factseekerbackend.domain.politician.repository.PoliticianRepository;
import com.factseekerbackend.domain.politician.repository.PoliticianTrustScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PoliticianService {

    private final PoliticianRepository repository;
    private final PoliticianTrustScoreRepository trustScoreRepository;

    public Page<PoliticianResponse> getPoliticians(String name, String party, Pageable pageable) {
        return repository.findAll(pageable).map(PoliticianResponse::from);
    }

    public PoliticianResponse getById(Long id) {
        Politician p = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Politician not found: " + id));
        return PoliticianResponse.from(p);
    }

    // 이름으로 한 명 (정확 일치 우선 -> 부분 일치)
    public PoliticianResponse getByName(String name) {
        String q = name == null ? "" : name.trim();
        if (q.isEmpty()) throw new IllegalArgumentException("Name is empty");

        Politician p = repository.findFirstByNameOrderByIdAsc(q)
                .or(() -> repository.findFirstByNameContainingIgnoreCase(q))
                .orElseThrow(() -> new IllegalArgumentException("Politician not found by name: " + name));
        return PoliticianResponse.from(p);
    }

    // 상위 12명 이름만 (overallScore 기준)
    public List<String> getTop12Names() {
        Page<PoliticianTrustScore> topScores = trustScoreRepository
                .findTop12ByOverallScoreOrderByDateDesc(PageRequest.of(0, 12));
        
        return topScores.getContent().stream()
                .map(score -> score.getPolitician().getName())
                .toList();
    }
}