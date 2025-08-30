package com.factseekerbackend.domain.youtube.controller.dto.response;

import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.factseekerbackend.global.util.TextSanitizer;

public record VideoDto(String videoId, String videoTitle, String thumbnailUrl, String channelId, String channelTitle) {

    public static VideoDto from(Video video) {
        VideoSnippet snippet = video != null ? video.getSnippet() : null;

        String id = video != null && video.getId() != null ? video.getId() : "";
        String rawTitle = (snippet != null && snippet.getTitle() != null) ? snippet.getTitle() : "";
        String title = TextSanitizer.sanitizeTitle(rawTitle);

        String thumbnailUrl = null;
        if (snippet != null && snippet.getThumbnails() != null && snippet.getThumbnails().getHigh() != null) {
            thumbnailUrl = snippet.getThumbnails().getHigh().getUrl();
        } else if (snippet != null && snippet.getThumbnails() != null && snippet.getThumbnails().getDefault() != null) {
            thumbnailUrl = snippet.getThumbnails().getDefault().getUrl();
        }

        String channelId = (snippet != null && snippet.getChannelId() != null) ? snippet.getChannelId() : "";
        String channelTitle = (snippet != null && snippet.getChannelTitle() != null) ? snippet.getChannelTitle() : "";

        return new VideoDto(id, title, thumbnailUrl, channelId, channelTitle);
    }
}
