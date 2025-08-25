package com.factseekerbackend.domain.youtube.controller.dto.response;

import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;

import java.util.Objects;

public record VideoDto(String videoId, String videoTitle, String thumbnailUrl, String channelId, String channelTitle) {

    public static VideoDto from(Video video) {
        VideoSnippet snippet = video.getSnippet();

        String id = video.getId();
        String title = (snippet != null && snippet.getTitle() != null) ? snippet.getTitle() : "";
        
        String thumbnailUrl = null;
        if (snippet != null && snippet.getThumbnails() != null && snippet.getThumbnails().getHigh() != null) {
            thumbnailUrl = snippet.getThumbnails().getHigh().getUrl();
        } else if (snippet != null && snippet.getThumbnails() != null && snippet.getThumbnails().getDefault() != null) {
            thumbnailUrl = snippet.getThumbnails().getDefault().getUrl();
        }
        String channelId = video.getSnippet() != null ? video.getSnippet().getChannelId() : null;
        String channelTitle = Objects.requireNonNull(video.getSnippet()).getChannelTitle();

        return new VideoDto(id, title, thumbnailUrl,channelId,channelTitle);
    }
}
