package com.factseekerbackend.domain.youtube.service;

import com.factseekerbackend.domain.youtube.controller.dto.response.VideoDto;
import com.factseekerbackend.domain.youtube.controller.dto.response.VideoListResponse;
import com.factseekerbackend.domain.youtube.controller.dto.response.YoutubeSearchResponse;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.factseekerbackend.domain.youtube.config.WhiteListChannels.WHITE_LIST_CHANNELS;

@Service
@RequiredArgsConstructor
@Transactional
public class YoutubeSearchService implements YoutubeService {

    private final YouTube youTube;
    private final OpenAiTitleFilterService filterService;

    @Value("${youtube.api.key}")
    private String apiKey;

       @Override
    public List<YoutubeSearchResponse> searchVideos(String query) throws IOException {
        YouTube.Search.List search = youTube.search().list(List.of("id", "snippet"));
        search.setKey(apiKey);
        search.setQ(query);
        search.setType(List.of("video"));
        search.setOrder("date");
        search.setVideoCategoryId("25");
        search.setMaxResults(10L);

        SearchListResponse response = search.execute();
        return response.getItems().stream()
                .map(YoutubeSearchResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public VideoListResponse getPopularPoliticsTop10Resp(long size) throws IOException {
        List<VideoDto> data = getWhiteListedPopular(size);
        return VideoListResponse.from(data, OffsetDateTime.now(ZoneId.of("Asia/Seoul")).toString());
    }

    private List<VideoDto> getWhiteListedPopular(long size) throws IOException {
        OffsetDateTime since = OffsetDateTime.now(ZoneId.of("Asia/Seoul")).minusDays(1);
        Set<String> videoIds = new LinkedHashSet<>();

        for (String channelId : WHITE_LIST_CHANNELS) {
            YouTube.Search.List s = youTube.search().list(List.of("id", "snippet"));
            s.setKey(apiKey);
            s.setType(List.of("video"));
            s.setChannelId(channelId);
            s.setOrder("viewCount");
            s.setPublishedAfter(since.toInstant().toString());
            s.setMaxResults(10L);
            SearchListResponse resp = s.execute();
            if (resp.getItems() == null) continue;
            for (SearchResult r : resp.getItems()) {
                if (r.getId() != null && r.getId().getVideoId() != null) {
                    videoIds.add(r.getId().getVideoId());
                }
            }
        }

        if (videoIds.isEmpty()) return List.of();

        List<Video> videos = fetchVideosByIds(new ArrayList<>(videoIds));

        // 길이 필터
        List<Video> lengthFiltered = new ArrayList<>(videos.stream()
                .filter(v -> {
                    long d = getDurationSecondsSafe(v);
                    return d >= 31 && d <= 1200;
                })
                .toList());

        if (lengthFiltered.isEmpty()) return List.of();

        // 조회수 내림차순 정렬
        lengthFiltered.sort((a, b) -> {
            BigInteger va = a.getStatistics() != null && a.getStatistics().getViewCount() != null ? a.getStatistics().getViewCount() : BigInteger.ZERO;
            BigInteger vb = b.getStatistics() != null && b.getStatistics().getViewCount() != null ? b.getStatistics().getViewCount() : BigInteger.ZERO;
            return vb.compareTo(va);
        });

        List<VideoDto> dtos = lengthFiltered.stream()
                .map(VideoDto::from)
                .toList();

        return classifyAndFilter(dtos, size);
    }

    private List<Video> fetchVideosByIds(List<String> ids) throws IOException {
        List<Video> out = new ArrayList<>();
        int BATCH = 50;
        for (int i = 0; i < ids.size(); i += BATCH) {
            List<String> slice = ids.subList(i, Math.min(ids.size(), i + BATCH));
            YouTube.Videos.List req = youTube.videos().list(List.of("id,snippet,statistics,contentDetails"));
            req.setKey(apiKey);
            req.setId(slice);
            var resp = req.execute();
            if (resp.getItems() != null) out.addAll(resp.getItems());
        }
        return out;
    }

    private List<VideoDto> classifyAndFilter(List<VideoDto> dtos, long size) {
        if (dtos == null || dtos.isEmpty()) return List.of();

        List<String> titles = dtos.stream().map(VideoDto::videoTitle).toList();
        List<Boolean> keep = filterService.arePoliticalTitles(titles);

        if (keep == null || keep.isEmpty()) {
            return dtos.stream().limit(size).toList();
        }

        ArrayList<VideoDto> out = new ArrayList<>((int) Math.min(size, dtos.size()));
        for (int i = 0; i < dtos.size(); i++) {
            if (i < keep.size() && Boolean.TRUE.equals(keep.get(i))) {
                out.add(dtos.get(i));
                if (out.size() >= size) break;
            }
        }
        return out;
    }

    @Override
    public VideoDto getVideoById(String videoId) throws IOException {
        YouTube.Videos.List request = youTube.videos()
                .list(List.of("id,snippet,statistics,contentDetails"));
        request.setKey(apiKey);
        request.setId(List.of(videoId));

        List<Video> items = request.execute().getItems();
        if (items == null || items.isEmpty()) {
            return null;
        }
        return VideoDto.from(items.getFirst());
    }

    private long getDurationSecondsSafe(Video video) {
        try {
            if (video == null || video.getContentDetails() == null || video.getContentDetails().getDuration() == null) {
                return -1;
            }
            return Duration.parse(video.getContentDetails().getDuration()).getSeconds();
        } catch (Exception e) {
            return -1;
        }
    }
}