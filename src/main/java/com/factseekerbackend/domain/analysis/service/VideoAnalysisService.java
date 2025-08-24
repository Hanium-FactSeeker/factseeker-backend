package com.factseekerbackend.domain.analysis.service;

import com.factseekerbackend.domain.analysis.controller.dto.request.VideoIdsRequest;
import com.factseekerbackend.domain.analysis.controller.dto.response.*;
import com.factseekerbackend.domain.analysis.controller.dto.response.fastapi.ClaimDto;
import com.factseekerbackend.domain.analysis.entity.Top10VideoAnalysis;
import com.factseekerbackend.domain.analysis.entity.VideoAnalysis;
import com.factseekerbackend.domain.analysis.entity.VideoAnalysisStatus;
import com.factseekerbackend.domain.analysis.repository.Top10VideoAnalysisRepository;
import com.factseekerbackend.domain.analysis.repository.VideoAnalysisRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class VideoAnalysisService {

    private final VideoAnalysisRepository repository;
    private final Top10VideoAnalysisRepository top10VideoAnalysisRepository;
    private final RedisTemplate<String, Object> cacheRedis;
    private final ObjectMapper om;

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
        return top10VideoAnalysisRepository.findById(videoId)
                .map(KeywordsResponse::from)
                .orElseThrow();
    }

    public boolean isInTop10(String videoId) {
        if (videoId == null || videoId.isBlank()) return false;
        List<String> ids = fetchTopNVideoIdsPipeline(10);
        return ids.contains(videoId);
    }

    public PercentStatusData getTop10VideosPercent(VideoIdsRequest request) {
        List<String> requestedVideoIds = request.videoIds();
        if (requestedVideoIds == null || requestedVideoIds.isEmpty()) {
            return PercentStatusData.builder().requested(0).completed(0).pending(0).failed(0).notFound(0).results(Collections.emptyList()).build();
        }

        // 1. Bulk fetch all existing analysis records from DB in one query
        Map<String, Top10VideoAnalysis> analysisMap = top10VideoAnalysisRepository.findAllById(requestedVideoIds)
                .stream()
                .collect(Collectors.toMap(Top10VideoAnalysis::getVideoId, analysis -> analysis));

        // 2. Fetch valid Top 10 IDs from Redis
        Set<String> validTop10Ids = new HashSet<>(fetchTopNVideoIdsPipeline(10));

        // 3. Process each ID with all data available locally
        List<PercentStatusResponse> results = new ArrayList<>();
        int completed = 0, pending = 0, failed = 0, notFound = 0;

        for (String videoId : requestedVideoIds) {
            Top10VideoAnalysis analysis = analysisMap.get(videoId);
            PercentStatusResponse result = new PercentStatusResponse(videoId);

            if (analysis != null) { // Case: Found in DB
                if (analysis.getStatus() == VideoAnalysisStatus.COMPLETED) {
                    result.status(VideoAnalysisStatus.COMPLETED).totalConfidenceScore(analysis.getTotalConfidenceScore());
                    completed++;
                } else { // FAILED
                    result.status(VideoAnalysisStatus.FAILED);
                    failed++;
                }
            } else { // Case: Not found in DB
                if (validTop10Ids.contains(videoId)) {
                    result.status(VideoAnalysisStatus.PENDING);
                    pending++;
                } else {
                    result.status(VideoAnalysisStatus.NOT_FOUND).message("해당 ID는 Top 10 목록에 없습니다.");
                    notFound++;
                }
            }
            results.add(result);
        }

        return PercentStatusData.builder()
                .requested(requestedVideoIds.size())
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
            } else if (o instanceof String) {
                String s = (String) o;
                if (!s.isBlank()) out.add(s);
            }
        }
        return new ArrayList<>(new LinkedHashSet<>(out));
    }

    private String rankKey(int rank) {
        return "popular:politics:KR:rank:" + rank;
    }
}
