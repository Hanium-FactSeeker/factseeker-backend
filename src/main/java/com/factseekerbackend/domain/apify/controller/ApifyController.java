package com.factseekerbackend.domain.apify.controller;

import com.factseekerbackend.domain.apify.dto.request.ApifyRequest;
import com.factseekerbackend.domain.apify.service.ApifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sns-posts")
@RequiredArgsConstructor
public class ApifyController {

    private final ApifyService apifyService;

    @PostMapping("/batch")
    public ResponseEntity<String> savePosts(@RequestBody List<ApifyRequest> posts) {
        apifyService.saveAll(posts);
        return ResponseEntity.ok("Saved " + posts.size() + " posts");
    }
}