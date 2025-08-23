package com.factseekerbackend.infrastructure.naver.newsapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class NewsResponse {
    private String lastBuildDate; //검색한 결과를 생성한 시간
    private int total; // 총 검색결과 갯수
    private int start; // 검색 시작 위치
    private int display; // 한번에 표시할 검색결과 갯수
    private List<NewsItem> items;
}
