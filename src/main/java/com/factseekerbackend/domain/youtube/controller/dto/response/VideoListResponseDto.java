package com.factseekerbackend.domain.youtube.controller.dto.response;

import java.util.List;

public record VideoListResponseDto(List<VideoDto> data, String timestamp) {


    public static VideoListResponseDto from(List<VideoDto> data, String timestamp) {
        return new VideoListResponseDto(data, timestamp);
    }

}
