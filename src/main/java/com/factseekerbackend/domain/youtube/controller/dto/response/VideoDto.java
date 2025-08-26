package com.factseekerbackend.domain.youtube.controller.dto.response;

import com.factseekerbackend.domain.youtube.util.YoutubePreprocessor;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record VideoDto(String videoId, String videoTitle, String thumbnailUrl, String channelId, String channelTitle) {

    public static VideoDto from(Video video) {
        VideoSnippet snippet = video.getSnippet();

        String id = video.getId();
        String titleRaw = (snippet != null && snippet.getTitle() != null) ? snippet.getTitle() : "";
        String title = YoutubePreprocessor.sanitizeDisplayText(titleRaw);

        String thumbnailUrl = getString(snippet);

        String channelId = snippet != null ? snippet.getChannelId() : null;
        String channelTitleRaw = snippet != null ? snippet.getChannelTitle() : "";
        String channelTitle = YoutubePreprocessor.sanitizeDisplayText(channelTitleRaw);

        return new VideoDto(id, title, thumbnailUrl, channelId, channelTitle);
    }

    @Nullable
    private static String getString(VideoSnippet snippet) {
        String thumbnailUrl = null;
        if (snippet != null && snippet.getThumbnails() != null) {
            if (snippet.getThumbnails().getMaxres() != null) {
                thumbnailUrl = snippet.getThumbnails().getMaxres().getUrl();
            } else if (snippet.getThumbnails().getHigh() != null) {
                thumbnailUrl = snippet.getThumbnails().getHigh().getUrl();
            } else if (snippet.getThumbnails().getMedium() != null) {
                thumbnailUrl = snippet.getThumbnails().getMedium().getUrl();
            } else if (snippet.getThumbnails().getDefault() != null) {
                thumbnailUrl = snippet.getThumbnails().getDefault().getUrl();
            }
        }
        return thumbnailUrl;
    }
}
