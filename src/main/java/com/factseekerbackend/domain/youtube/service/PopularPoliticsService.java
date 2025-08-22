package com.factseekerbackend.domain.youtube.service;

import com.factseekerbackend.domain.youtube.controller.dto.response.VideoDto;
import com.factseekerbackend.domain.youtube.controller.dto.response.VideoListResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularPoliticsService {

    private final YoutubeService youtubeService;
    private final ObjectMapper objectMapper;

    @Qualifier("cacheRedisTemplate")
    private final RedisTemplate<String, Object> cacheRedis;

    private static final String KEY_PREFIX = "popular:politics:KR";
    private static final Duration TTL = Duration.ofMinutes(70);

    public VideoListResponseDto getPopularPolitics(int size) {
        List<VideoDto> data = readFromRedis(size);
        String timestamp = getTimestampFromRedis();

        if (data.size() < size || timestamp == null) {
            log.info("Popular politics cache miss or incomplete. Refreshing cache for size: {}", size);
            try {
                VideoListResponseDto refreshedData = refreshTopN(size);
                data = refreshedData.data();
                timestamp = refreshedData.timestamp();
            } catch (Exception e) {
                log.error("Failed to refresh popular politics cache. Returning empty list.", e);
                return VideoListResponseDto.from(List.of(), nowSeoul());
            }
        }

        return VideoListResponseDto.from(data, timestamp);
    }

    public VideoListResponseDto refreshTopN(int size) throws IOException {
        log.info("Fetching top {} popular politics videos from YouTube service.", size);
        VideoListResponseDto fetchedDto = youtubeService.getPopularPoliticsTop10Resp(size);
        List<VideoDto> videoList = (fetchedDto != null && fetchedDto.data() != null) ? fetchedDto.data() : List.of();

        if (videoList.isEmpty()) {
            log.warn("Fetched popular politics list from YouTube is empty. Cache will not be updated.");
            return VideoListResponseDto.from(List.of(), nowSeoul());
        }

        log.info("Updating Redis cache with {} videos.", videoList.size());
        
        HashOperations<String, Object, Object> hashOps = cacheRedis.opsForHash();

        for (int i = 0; i < videoList.size(); i++) {
            int rank = i + 1;
            VideoDto video = videoList.get(i);
            String key = rankKey(rank);
            
            Map<String, Object> videoMap = Map.of(
                "videoId", video.videoId(),
                "videoTitle", video.videoTitle(),
                "thumbnailUrl", video.thumbnailUrl() != null ? video.thumbnailUrl() : "",
                "updatedAt", fetchedDto.timestamp()
            );

            hashOps.putAll(key, videoMap);
            cacheRedis.expire(key, TTL);
        }
        
        for (int i = videoList.size(); i < 20; i++) {
             int rank = i + 1;
             cacheRedis.delete(rankKey(rank));
        }

        log.info("Successfully updated Redis cache for popular politics videos.");

        return fetchedDto;
    }

    private List<VideoDto> readFromRedis(int size) {
        HashOperations<String, Object, Object> hashOps = cacheRedis.opsForHash();
        List<VideoDto> data = new ArrayList<>(size);

        for (int rank = 1; rank <= size; rank++) {
            String key = rankKey(rank);
            Map<Object, Object> videoMap = hashOps.entries(key);

            if (!videoMap.isEmpty() && videoMap.get("videoId") != null) {
                try {
                    VideoDto dto = objectMapper.convertValue(videoMap, VideoDto.class);
                    data.add(dto);
                } catch (IllegalArgumentException e) {
                    log.error("Failed to convert Redis hash to VideoDto for key: {}", key, e);
                }
            } else {
                break;
            }
        }
        return data;
    }
    
    private String getTimestampFromRedis() {
        String rankOneKey = rankKey(1);
        Long ttl = cacheRedis.getExpire(rankOneKey, TimeUnit.SECONDS);
        if (ttl != null && ttl > 0) {
            return (String) cacheRedis.opsForHash().get(rankOneKey, "updatedAt");
        }
        return null;
    }

    private String rankKey(int rank) {
        return KEY_PREFIX + ":rank:" + rank;
    }

    private String nowSeoul() {
        return OffsetDateTime.now(ZoneId.of("Asia/Seoul")).toString();
    }
}