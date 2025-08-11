package com.factseekerbackend.domain.youtube.controller.dto.response;

import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;


public record VideoDto(String videoId, String videoTitle, ThumbnailDetails thumbnailDetails) {

    public static VideoDto from(Video video) {
        VideoSnippet snippet = video.getSnippet();

        String id = video.getId();
        String title = (snippet != null && snippet.getTitle() != null) ? snippet.getTitle() : "";
        ThumbnailDetails thumbnails = snippet != null ? snippet.getThumbnails() : null;

        return new VideoDto(id, title, thumbnails);
    }
}
