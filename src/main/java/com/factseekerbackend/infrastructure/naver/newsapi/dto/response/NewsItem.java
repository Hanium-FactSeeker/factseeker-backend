package com.factseekerbackend.infrastructure.naver.newsapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class NewsItem {
    private String title; //제목
    private String link; //뉴스 기사의 네이버 뉴스 URL. 네이버에 제공되지 않은 기사라면 기사 원문의 URL을 반환합니다.
    private String description; // 뉴스기사 내용을 요약한 정보
    private String pubDate;
}
