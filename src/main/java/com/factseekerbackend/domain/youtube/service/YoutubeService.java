package com.factseekerbackend.domain.youtube.service;

import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.List;

public interface YoutubeService {
    public List<SearchResult> searchVideos(String query) throws IOException;
}
