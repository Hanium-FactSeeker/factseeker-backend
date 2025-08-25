package com.factseekerbackend.domain.youtube.service;

import com.factseekerbackend.domain.youtube.controller.dto.response.VideoListResponse;
import com.factseekerbackend.domain.youtube.controller.dto.response.YoutubeSearchResponse;

import java.io.IOException;
import java.util.List;

public interface YoutubeService {
    List<YoutubeSearchResponse> searchVideos(String query) throws IOException;
    VideoListResponse getPopularPoliticsTop10Resp(long size) throws IOException;

}