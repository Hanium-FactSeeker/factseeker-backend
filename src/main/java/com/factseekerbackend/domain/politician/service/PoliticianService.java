package com.factseekerbackend.domain.politician.service;

import com.factseekerbackend.domain.politician.dto.SortType;
import com.factseekerbackend.domain.politician.dto.response.PoliticianResponse;
import com.factseekerbackend.domain.politician.dto.response.PoliticianWithScore;
import com.factseekerbackend.domain.politician.dto.response.TrustScoreResponse;
import com.factseekerbackend.domain.politician.entity.Politician;
import com.factseekerbackend.domain.politician.entity.PoliticianTrustScore;
import com.factseekerbackend.domain.politician.repository.PoliticianRepository;
import com.factseekerbackend.domain.politician.repository.PoliticianTrustScoreRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
  public List<PoliticianResponse> searchByName(String name) {
    String q = name == null ? "" : name.trim();
    if (q.isEmpty()) {
      // 검색어가 없으면 빈 리스트 반환
      return Collections.emptyList();
    }

    // [변경] 부분 일치하는 모든 Politician 엔티티를 조회
    List<Politician> politicians = repository.findByNameContainingIgnoreCase(q);

    // 조회된 엔티티 리스트를 DTO(PoliticianResponse) 리스트로 변환하여 반환
    return politicians.stream()
        .map(PoliticianResponse::from)
        .collect(Collectors.toList());
  }

  // 상위 12명 이름과 점수 (overallScore 기준)
  public List<PoliticianWithScore> getTop12NamesWithScores() {
    Page<PoliticianTrustScore> topScores = trustScoreRepository
        .findTop12ByOverallScoreOrderByDateDesc(PageRequest.of(0, 12));

    return topScores.getContent().stream()
        .map(score -> new PoliticianWithScore(
            score.getPolitician().getName(),
            score.getPolitician().getParty(),
            score.getGeminiScore(),
            score.getGptScore(),
            score.getOverallScore()
        ))
        .toList();
  }

  // 전체 정치인 페이지네이션 조회
  public Page<PoliticianResponse> getAllPoliticiansPaged(int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
    return repository.findAll(pageable)
        .map(PoliticianResponse::from);
  }

  // 신뢰도 점수 페이지네이션 조회 (정렬 옵션 포함)
  public Page<PoliticianWithScore> getTrustScoresPaged(int page, int size, SortType sortType) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PoliticianTrustScore> trustScores;

    // 정렬 방식에 따라 다른 쿼리 호출
    switch (sortType) {
      case LATEST:
        // 최신순 정렬 (업데이트 날짜 기준)
        trustScores = trustScoreRepository.findLatestCompletedScoresOrderByLatest(pageable);
        break;
      case TRUST_SCORE:
      default:
        // 신뢰도순 정렬 (기본값)
        trustScores = trustScoreRepository.findLatestCompletedScoresOrderByTrustScore(pageable);
        break;
    }

    // PoliticianTrustScore를 PoliticianWithScore로 수동으로 변환합니다.
    return trustScores.map(PoliticianWithScore::from);
  }

  public TrustScoreResponse getLatestScoreByPoliticianId(Long politicianId) {
    PoliticianTrustScore latestScore = trustScoreRepository.findLatestCompletedScore(politicianId)
        .orElseThrow(() -> new EntityNotFoundException("ID " + politicianId + "에 대한 신뢰도 분석 결과를 찾을 수 없습니다."));
    return TrustScoreResponse.from(latestScore);
  }

  public List<TrustScoreResponse> getLatestScoresByName(String name) {
    String q = name == null ? "" : name.trim();
    if (q.isEmpty()) {
      return Collections.emptyList();
    }

    List<Politician> politicians = repository.findByNameContainingIgnoreCase(q);

    return politicians.stream()
        .map(politician -> trustScoreRepository.findLatestCompletedScore(politician.getId()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(TrustScoreResponse::from)
        .collect(Collectors.toList());
  }

}
