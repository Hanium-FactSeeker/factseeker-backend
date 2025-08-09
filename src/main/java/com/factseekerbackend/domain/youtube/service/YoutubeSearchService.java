package com.factseekerbackend.domain.youtube.service;


import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
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
        search.setType(List.of("video"));     // "video" 고정
        search.setMaxResults(10L);

        SearchListResponse response = search.execute();
        return response.getItems();  // List<com.google.api.services.youtube.model.SearchResult>
    }
}