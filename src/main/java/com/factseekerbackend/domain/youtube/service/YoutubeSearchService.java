package com.factseekerbackend.domain.youtube.service;


import com.factseekerbackend.domain.youtube.controller.dto.response.VideoDto;
import com.factseekerbackend.domain.youtube.controller.dto.response.VideoListResponseDto;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class YoutubeSearchService implements YoutubeService {

    private final YouTube youTube;

    @Value("${youtube.api.key}")
    private String apiKey;

    @Override
    public List<SearchResult> searchVideos(String query) throws IOException {
        YouTube.Search.List search = youTube.search().list(List.of("id","snippet"));
        search.setKey(apiKey);
        search.setQ(query);
        search.setType(List.of("video"));
        search.setMaxResults(10L);

        SearchListResponse response = search.execute();
        return response.getItems();
    }

    public VideoListResponseDto getPopularPoliticsTop10Resp(long size) throws IOException {
        List<VideoDto> data = getPopularPoliticsTop10(size);
        return VideoListResponseDto.from(data, data.size());
    }

    private List<VideoDto> getPopularPoliticsTop10(long size) throws IOException {
        YouTube.Videos.List request = youTube.videos()
                .list(List.of("id,snippet,statistics,contentDetails"));

        request.setKey(apiKey);
        request.setChart("mostPopular");
        request.setRegionCode("KR");
        request.setVideoCategoryId("25"); // 정치/뉴스 카테고리
        request.setMaxResults(size);

        List<Video> items = request.execute().getItems();

        return items.stream()
                .map(VideoDto::from)
                .toList();
    }



}