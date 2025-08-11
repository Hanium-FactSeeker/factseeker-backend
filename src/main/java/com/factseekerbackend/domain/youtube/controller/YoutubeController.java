package com.factseekerbackend.domain.youtube.controller;

import com.factseekerbackend.domain.youtube.controller.dto.response.VideoListResponseDto;
import com.factseekerbackend.domain.youtube.service.YoutubeService;
import com.google.api.services.youtube.model.SearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
public class YoutubeController {
    private final YoutubeService youtubeService;

    @GetMapping("/search")
    public List<SearchResult> searchVideos(@RequestParam("query") String query) throws IOException {
        return youtubeService.searchVideos(query);
    }

    @GetMapping("/popular-politics")
    public VideoListResponseDto getPopularPolitics(@RequestParam(value = "size", defaultValue = "10") long size) throws IOException {
        return youtubeService.getPopularPoliticsTop10Resp(size);
    }
}
