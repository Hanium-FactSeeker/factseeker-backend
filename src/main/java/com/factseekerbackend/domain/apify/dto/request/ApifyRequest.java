package com.factseekerbackend.domain.apify.dto.request;

public record ApifyRequest(
        String username,      // SNS 계정
        String full_text,     // 게시글 본문
        String date           // 게시일자
) {}
