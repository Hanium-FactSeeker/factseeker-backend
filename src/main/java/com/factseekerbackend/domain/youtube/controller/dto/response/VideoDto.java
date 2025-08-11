package com.factseekerbackend.domain.youtube.controller.dto.response;

import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;


public record VideoDto(String videoId, String videoTitle, PlayerOptionsDto playerOptions) {

    public static VideoDto from(String videoId, String videoTitle) {
        return new VideoDto(videoId, videoTitle, PlayerOptionsDto.from());
    }

    public static VideoDto from(Video video) {
        VideoSnippet snippet = video.getSnippet();

        String id = video.getId();
        String title = (snippet != null && snippet.getTitle() != null) ? snippet.getTitle() : "";

        return new VideoDto(id, title, PlayerOptionsDto.from());
    }
}
