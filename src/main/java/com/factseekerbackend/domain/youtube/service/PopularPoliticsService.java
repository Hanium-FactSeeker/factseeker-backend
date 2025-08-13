package com.factseekerbackend.domain.youtube.service;

import com.factseekerbackend.domain.youtube.controller.dto.response.VideoDto;
import com.factseekerbackend.domain.youtube.controller.dto.response.VideoListResponseDto;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PopularPoliticsService {

    private final YoutubeService youtubeService;

    @Qualifier("cacheRedisTemplate")
    private final RedisTemplate<String, Object> cacheRedis;

    private static final String KEY_PREFIX = "popular:politics:KR";
    private static final Duration TTL = Duration.ofMinutes(70);

    /**
     * 프론트 엔드포인트에서 호출:
     * - Redis 해시에서 순위별로 읽어 리스트 구성
     * - timestamp는 Redis의 updatedAt(보통 rank:1)에 저장된 값 사용
     * - 캐시 미스/불완전 시 YouTube로 보충(refreshTopN) 후 다시 Redis에서 읽어 반환
     */
    public VideoListResponseDto getPopularPolitics(int size) {
        HashOperations<String, Object, Object> hashOps = cacheRedis.opsForHash();

        // 1) timestamp(=updatedAt)는 rank:1 해시에서 한 번만 읽음
        String timestampFromRedis = (String) hashOps.get(rankKey(1), "updatedAt");

        // 2) 리스트 조립
        List<VideoDto> data = readFromRedis(size);

        // 3) 캐시 미스/불완전 → 보충하고 다시 읽기 (timestamp도 항상 Redis에서만 읽음)
        if (data.size() < size || timestampFromRedis == null) {
            refreshTopN(size);
            timestampFromRedis = (String) hashOps.get(rankKey(1), "updatedAt");
            data = readFromRedis(size);
        }

        // 4) 최종 응답
        return VideoListResponseDto.from(
                data,
                (timestampFromRedis != null) ? timestampFromRedis : nowSeoul() // 안전망
        );
    }

    /**
     * 스케줄러/보충 공용:
     * - YouTube에서 TopN 리스트를 받아옴
     * - 이번 갱신 시각(ts)을 한 번 생성
     * - Redis 해시(popular:politics:KR:rank:{n})에 videoId/videoTitle/thumbnailUrl/updatedAt 저장
     * - 응답의 timestamp도 동일한 ts로 반환
     */
    public void refreshTopN(int size) {
        try {
            VideoListResponseDto fetched = youtubeService.getPopularPoliticsTop10Resp(size);
            List<VideoDto> list = (fetched != null && fetched.data() != null) ? fetched.data() : List.of();

            // 이번 갱신 시각(=업데이트 시간) — 단 한 번 생성
            String ts = nowSeoul();

            HashOperations<String, Object, Object> hashOps = cacheRedis.opsForHash();
            int limit = Math.min(list.size(), size);

            for (int i = 0; i < limit; i++) {
                int rank = i + 1;
                VideoDto video = list.get(i);

                String id = video.videoId();
                String title = video.videoTitle();

                String thumb = extractThumbUrl(video);
                if (thumb == null || thumb.isBlank()) {
                    thumb = "https://i.ytimg.com/vi/" + id + "/hqdefault.jpg";
                }

                String key = rankKey(rank);
                hashOps.put(key, "videoId", id);
                hashOps.put(key, "videoTitle", title);
                hashOps.put(key, "thumbnailUrl", thumb);
                hashOps.put(key, "updatedAt", ts);
                cacheRedis.expire(key, TTL);
            }

            // 응답도 같은 ts 사용
            VideoListResponseDto.from(list, ts);

        } catch (Exception e) {
            // 실패 시 빈 리스트 + now 반환(로그는 상황에 맞게 추가)
            VideoListResponseDto.from(List.of(), nowSeoul());
        }
    }


    private List<VideoDto> readFromRedis(int size) {
        HashOperations<String, Object, Object> hashOps = cacheRedis.opsForHash();
        ArrayList<VideoDto> data = new ArrayList<>(size);

        for (int rank = 1; rank <= size; rank++) {
            String key = rankKey(rank);
            Map<Object, Object> m = hashOps.entries(key);

            if (!m.isEmpty()) {
                String id = (String) m.get("videoId");
                String title = (String) m.get("videoTitle");
                String thumbUrl = (String) m.get("thumbnailUrl");
                if (id != null && title != null) {
                    data.add(new VideoDto(id, title, toThumbnailDetails(thumbUrl)));
                }
            }
        }
        return data;
    }

    private String rankKey(int rank) {
        return KEY_PREFIX + ":rank:" + rank;
    }

    private String nowSeoul() {
        return java.time.OffsetDateTime.now(java.time.ZoneId.of("Asia/Seoul")).toString();
    }

    private String extractThumbUrl(VideoDto v) {
        if (v == null || v.thumbnailDetails() == null) return null;
        ThumbnailDetails td = v.thumbnailDetails();
        if (td.getMaxres() != null && td.getMaxres().getUrl() != null) return td.getMaxres().getUrl();
        if (td.getStandard() != null && td.getStandard().getUrl() != null) return td.getStandard().getUrl();
        if (td.getHigh() != null && td.getHigh().getUrl() != null) return td.getHigh().getUrl();
        if (td.getMedium() != null && td.getMedium().getUrl() != null) return td.getMedium().getUrl();
        if (td.getDefault() != null && td.getDefault().getUrl() != null) return td.getDefault().getUrl();
        return null;
    }

    private ThumbnailDetails toThumbnailDetails(String url) {
        if (url == null || url.isBlank()) return null;
        Thumbnail h = new Thumbnail();
        h.setUrl(url);
        ThumbnailDetails td = new ThumbnailDetails();
        td.setHigh(h);
        return td;
    }
}
