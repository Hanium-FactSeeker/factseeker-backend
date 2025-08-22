package com.factseekerbackend.infrastructure.naver.newsapi.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Getter
public class NewsRequest {
    private final String query;   // 필수 (생성자로 주입)
    private final int display;    // 기본 10, 범위 10~100
    private final int start;      // 기본 1, 범위 1~1000
    private final String sort;    // sim | date

    public NewsRequest(String query) {
        this(query, 10, 1, "date"); // 기본값 세팅
    }

    public NewsRequest(String query, int display, int start, String sort) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query is required");
        }
        if (display < 10 || display > 100) {
            throw new IllegalArgumentException("display must be between 10 and 100");
        }
        if (start < 1 || start > 1000) {
            throw new IllegalArgumentException("start must be between 1 and 1000");
        }
        if (!(sort.equals("sim") || sort.equals("date"))) {
            throw new IllegalArgumentException("sort must be 'sim' or 'date'");
        }

        this.query = query;
        this.display = display;
        this.start = start;
        this.sort = sort;
    }

    public MultiValueMap<String, String> toMultiValueMap() {
        var map = new LinkedMultiValueMap<String, String>();
        map.add("query", query);
        map.add("display", String.valueOf(display));
        map.add("start", String.valueOf(start));
        map.add("sort", sort);
        return map;
    }

}
