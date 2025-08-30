package com.factseekerbackend.domain.analysis.service;

import com.factseekerbackend.domain.analysis.controller.dto.response.*;
import com.factseekerbackend.domain.analysis.controller.dto.response.fastapi.ClaimDto;
import com.factseekerbackend.domain.analysis.entity.video.Top10VideoAnalysis;
import com.factseekerbackend.domain.analysis.entity.video.VideoAnalysis;
import com.factseekerbackend.domain.analysis.entity.AnalysisStatus;
import com.factseekerbackend.domain.analysis.repository.Top10VideoAnalysisRepository;
import com.factseekerbackend.domain.analysis.repository.VideoAnalysisRepository;
import com.factseekerbackend.domain.youtube.controller.dto.response.VideoDto;
import com.factseekerbackend.domain.youtube.service.YoutubeService;
import com.factseekerbackend.global.exception.BusinessException;
import com.factseekerbackend.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class VideoAnalysisService {

    private final VideoAnalysisRepository repository;
    private final Top10VideoAnalysisRepository top10VideoAnalysisRepository;
    @Qualifier("cacheRedisTemplate")
    private final RedisTemplate<String, Object> cacheRedis;
    private final ObjectMapper om;
    private final YoutubeService youtubeService;

    public VideoAnalysisResponse getVideoAnalysis(Long userId, Long videoAnalysisId) {
        VideoAnalysis videoAnalysis = repository.findByUserIdAndId(userId, videoAnalysisId)
                .orElseThrow(() -> new NullPointerException("해당 비디오를 찾을 수 없습니다."));

        Object claims = parseClaims(videoAnalysis.getClaims());

        return VideoAnalysisResponse.from(videoAnalysis, claims);
    }

    private Object parseClaims(String claimsJson) {
        if (claimsJson == null || claimsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return om.readValue(claimsJson, new TypeReference<List<ClaimDto>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse claims JSON: {}", claimsJson, e);
            return Collections.emptyList();
        }
    }

    public KeywordsResponse getTop10YoutubeKeywords(String videoId) {
        Top10VideoAnalysis analysis = top10VideoAnalysisRepository.findById(videoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VIDEO_NOT_FOUND, ErrorCode.VIDEO_NOT_FOUND.getMessage()));
        if (analysis.getStatus() == AnalysisStatus.FAILED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "분석이 실패하여 키워드를 제공할 수 없습니다.");
        }
        return KeywordsResponse.from(analysis);
    }

    public boolean isInTop10(String videoId) {
        if (videoId == null || videoId.isBlank()) return false;
        List<String> ids = fetchTopNVideoIdsPipeline(10);
        return ids.contains(videoId);
    }

    public PercentStatusData getTop10VideosPercent(List<String> request) {

        if (request == null || request.isEmpty()) {
            return PercentStatusData.builder().requested(0).completed(0).pending(0).failed(0).notFound(0).results(Collections.emptyList()).build();
        }

        // 1. Bulk fetch all existing analysis records from DB in one query
        Map<String, Top10VideoAnalysis> analysisMap = top10VideoAnalysisRepository.findAllById(request)
                .stream()
                .collect(Collectors.toMap(Top10VideoAnalysis::getVideoId, analysis -> analysis));

        // 2. Fetch valid Top 10 IDs from Redis
        Set<String> validTop10Ids = new HashSet<>(fetchTopNVideoIdsPipeline(10));

        // 3. Process each ID with all data available locally
        List<PercentStatusResponse> results = new ArrayList<>();
        int completed = 0, pending = 0, failed = 0, notFound = 0;

        for (String videoId : request) {
            Top10VideoAnalysis analysis = analysisMap.get(videoId);
            PercentStatusResponse result = new PercentStatusResponse(videoId);

            if (analysis != null) { // Case: Found in DB
                AnalysisStatus st = analysis.getStatus();
                if (st == AnalysisStatus.COMPLETED) {
                    result.status(AnalysisStatus.COMPLETED)
                            .totalConfidenceScore(analysis.getTotalConfidenceScore());
                    completed++;
                } else if (st == AnalysisStatus.PENDING) {
                    result.status(AnalysisStatus.PENDING);
                    pending++;
                } else if (st == AnalysisStatus.FAILED) {
                    result.status(AnalysisStatus.FAILED);
                    failed++;
                } else { // null or NOT_FOUND
                    if (validTop10Ids.contains(videoId)) {
                        result.status(AnalysisStatus.PENDING);
                        pending++;
                    } else {
                        result.status(AnalysisStatus.NOT_FOUND).message("해당 ID는 Top 10 목록에 없습니다.");
                        notFound++;
                    }
                }
            } else { // Case: Not found in DB
                if (validTop10Ids.contains(videoId)) {
                    result.status(AnalysisStatus.PENDING);
                    pending++;
                } else {
                    result.status(AnalysisStatus.NOT_FOUND).message("해당 ID는 Top 10 목록에 없습니다.");
                    notFound++;
                }
            }
            results.add(result);
        }

        return PercentStatusData.builder()
                .requested(request.size())
                .completed(completed)
                .pending(pending)
                .failed(failed)
                .notFound(notFound)
                .results(results)
                .build();
    }

    private List<String> fetchTopNVideoIdsPipeline(int n) {
        List<Object> raw = cacheRedis.executePipelined((RedisCallback<Object>) connection -> {
            for (int rank = 1; rank <= n; rank++) {
                byte[] key = cacheRedis.getStringSerializer().serialize(rankKey(rank));
                byte[] field = cacheRedis.getStringSerializer().serialize("videoId");
                connection.hashCommands().hGet(key, field);
            }
            return null;
        });

        List<String> out = new ArrayList<>(n);
        for (Object o : raw) {
            if (o instanceof byte[]) {
                String id = cacheRedis.getStringSerializer().deserialize((byte[]) o);
                if (id != null && !id.isBlank()) out.add(id);
            } else if (o instanceof String s) {
                if (!s.isBlank()) out.add(s);
            }
        }
        return new ArrayList<>(new LinkedHashSet<>(out));
    }

    private String rankKey(int rank) {
        return "popular:politics:KR:rank:" + rank;
    }

    public List<RecentAnalysisVideoResponse> getThreeRecentVideos(Long userId) {
        return repository.findTop3ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(videoAnalysis -> {
                    try {
                        VideoDto videoDto = youtubeService.getVideoById(videoAnalysis.getVideoId());
                        String title = (videoDto != null && videoDto.videoTitle() != null) ? videoDto.videoTitle() : "";
                        return RecentAnalysisVideoResponse.from(videoAnalysis, title);
                    } catch (IOException e) {
                        // 메인 안정성: 개별 실패는 스킵하고 가능한 항목만 반환
                        log.warn("유튜브 메타데이터 조회 실패, 해당 항목 건너뜀. videoId={}, analysisId={}",
                                videoAnalysis.getVideoId(), videoAnalysis.getId(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
