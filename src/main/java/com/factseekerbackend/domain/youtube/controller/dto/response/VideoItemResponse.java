package com.factseekerbackend.domain.youtube.controller.dto.response;

public record VideoItemResponse(VideoDto data, String timestamp, int rank) {

    public static VideoItemResponse from(VideoDto data, String timestamp, int rank) {
        return new VideoItemResponse(data, timestamp, rank);
    }
}

