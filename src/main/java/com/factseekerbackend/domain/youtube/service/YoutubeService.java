package com.factseekerbackend.domain.youtube.service;

import com.factseekerbackend.domain.youtube.controller.dto.response.VideoDto;
import com.factseekerbackend.domain.youtube.controller.dto.response.VideoListResponseDto;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.List;

public interface YoutubeService {
    public List<SearchResult> searchVideos(String query) throws IOException;
    public VideoListResponseDto getPopularPoliticsTop10Resp(long size) throws IOException;

}
