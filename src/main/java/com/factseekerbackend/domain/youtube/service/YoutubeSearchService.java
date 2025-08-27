package com.factseekerbackend.domain.youtube.service;


import com.factseekerbackend.domain.youtube.controller.dto.response.VideoDto;
import com.factseekerbackend.domain.youtube.controller.dto.response.VideoListResponse;
import com.factseekerbackend.domain.youtube.controller.dto.response.YoutubeSearchResponse;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class YoutubeSearchService implements YoutubeService {

    private final YouTube youTube;

    @Value("${youtube.api.key}")
    private String apiKey;

    @Override
    public List<YoutubeSearchResponse> searchVideos(String query) throws IOException {
        YouTube.Search.List search = youTube.search().list(List.of("id","snippet"));
        search.setKey(apiKey);
        search.setQ(query);
        search.setType(List.of("video"));
        search.setMaxResults(10L);

        SearchListResponse response = search.execute();
        return response.getItems().stream()
                .map(YoutubeSearchResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public VideoListResponse getPopularPoliticsTop10Resp(long size) throws IOException {
        List<VideoDto> data = getPopularPoliticsTop10(size);
        return VideoListResponse.from(data, OffsetDateTime.now(ZoneId.of("Asia/Seoul")).toString());
    }

    private List<VideoDto> getPopularPoliticsTop10(long size) throws IOException {
        YouTube.Videos.List request = youTube.videos()
                .list(List.of("id,snippet,statistics,contentDetails"));

        request.setKey(apiKey);
        request.setChart("mostPopular");
        request.setRegionCode("KR");
        request.setVideoCategoryId("25"); // 뉴스/정치
        request.setVideoDuration("medium"); // 쇼츠 제외 (4분 이상)
        request.setMaxResults(size);

        List<Video> items = request.execute().getItems();
        return items.stream()
                .map(VideoDto::from)
                .toList();
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
        return VideoDto.from(items.get(0));
    }
}
