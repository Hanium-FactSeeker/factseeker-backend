package com.factseekerbackend.domain.youtube.controller;

import com.factseekerbackend.domain.youtube.controller.dto.response.VideoListResponseDto;
import com.factseekerbackend.domain.youtube.service.PopularPoliticsService;
import com.factseekerbackend.domain.youtube.service.YoutubeService;
import com.google.api.services.youtube.model.SearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
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
    private final PopularPoliticsService popularPoliticsService;

    @GetMapping("/videos")
    public ResponseEntity<VideoListResponseDto> popularPolitics(@RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(popularPoliticsService.getPopularPolitics(size));
    }

}
