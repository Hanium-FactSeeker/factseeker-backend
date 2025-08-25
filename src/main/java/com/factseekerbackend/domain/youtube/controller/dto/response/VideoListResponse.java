package com.factseekerbackend.domain.youtube.controller.dto.response;

import java.util.List;

public record VideoListResponse(List<VideoDto> data, String timestamp) {


    public static VideoListResponse from(List<VideoDto> data, String timestamp) {
        return new VideoListResponse(data, timestamp);
    }

}
