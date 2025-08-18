package com.factseekerbackend.domain.politician.controller;

import com.factseekerbackend.domain.politician.dto.request.PoliticianNameRequest;
import com.factseekerbackend.domain.politician.dto.response.PoliticianResponse;
import com.factseekerbackend.domain.politician.service.PoliticianService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/politicians")
@RequiredArgsConstructor
public class PoliticianController {

    private final PoliticianService service;

    // 상위 12명 이름만 (객체로 감싸서 반환)
    @GetMapping("/top12-names")
    public TopNamesResponse top12Names() {
        return new TopNamesResponse(service.getTop12Names());
    }

    // id로 조회 - 숫자만 매칭되도록 정규식 명시
    @GetMapping("/{id:\\d+}")
    public PoliticianResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // 이름으로 조회 - POST + JSON Body
    @PostMapping("/by-name")
    public PoliticianResponse getByNameBody(@RequestBody PoliticianNameRequest request) {
        return service.getByName(request.name());
    }

    // Response wrapper for top12
    public record TopNamesResponse(List<String> names) {}
}