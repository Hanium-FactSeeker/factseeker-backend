package com.factseekerbackend.domain.politician.service;

import com.factseekerbackend.domain.politician.dto.SortType;
import com.factseekerbackend.domain.politician.dto.response.PoliticianResponse;
import com.factseekerbackend.domain.politician.dto.response.PoliticianWithScore;
import com.factseekerbackend.domain.politician.entity.Politician;
import com.factseekerbackend.domain.politician.entity.PoliticianTrustScore;
import com.factseekerbackend.domain.politician.repository.PoliticianRepository;
import com.factseekerbackend.domain.politician.repository.PoliticianTrustScoreRepository;
import java.util.List;
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
  public PoliticianResponse getByName(String name) {
    String q = name == null ? "" : name.trim();
    if (q.isEmpty()) throw new IllegalArgumentException("Name is empty");

    Politician p = repository.findFirstByNameOrderByIdAsc(q)
        .or(() -> repository.findFirstByNameContainingIgnoreCase(q))
        .orElseThrow(() -> new IllegalArgumentException("Politician not found by name: " + name));
    return PoliticianResponse.from(p);
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

  // 페이지네이션된 정치인 점수 조회 (3명씩)
  public Page<PoliticianWithScore> getTopPoliticiansWithScores(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PoliticianTrustScore> topScores = trustScoreRepository
        .findTop12ByOverallScoreOrderByDateDesc(pageable);

    return topScores.map(score -> new PoliticianWithScore(
        score.getPolitician().getName(),
        score.getPolitician().getParty(),
        score.getGeminiScore(),
        score.getGptScore(),
        score.getOverallScore()
    ));
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

}
