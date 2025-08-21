package com.factseekerbackend.global.politicalTranding.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MockTrendController {

    @GetMapping("/api/trends")
    public Map<String, String> getTrends() {
        return Map.of("trend", "임시 정치 트렌드입니다");
    }
}
