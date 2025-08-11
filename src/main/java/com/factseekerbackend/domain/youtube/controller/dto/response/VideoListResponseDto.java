package com.factseekerbackend.domain.youtube.controller.dto.response;

import java.util.List;

public record VideoListResponseDto(List<VideoDto> data, int count, String timestamp) {


    public static VideoListResponseDto from(List<VideoDto> data, int count) {
        return new VideoListResponseDto(data, count, java.time.OffsetDateTime.now(java.time.ZoneId.of("Asia/Seoul")).toString());
    }

}
