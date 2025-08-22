package com.factseekerbackend.domain.youtube.controller.dto.response;

import com.google.api.services.youtube.model.SearchResult;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class YoutubeSearchResponse {

    private String url;
    private String videoTitle;
    private String thumbnailUrl;
    private String updatedAt;

    @Builder
    public YoutubeSearchResponse(String url, String videoTitle, String thumbnailUrl, String updatedAt) {
        this.url = url;
        this.videoTitle = videoTitle;
        this.thumbnailUrl = thumbnailUrl;
        this.updatedAt = updatedAt;
    }

    public static YoutubeSearchResponse from(SearchResult searchResult) {
        String thumbnailUrl = null;
        if (searchResult.getSnippet() != null && searchResult.getSnippet().getThumbnails() != null) {
            if (searchResult.getSnippet().getThumbnails().getHigh() != null) {
                thumbnailUrl = searchResult.getSnippet().getThumbnails().getHigh().getUrl();
            } else if (searchResult.getSnippet().getThumbnails().getDefault() != null) {
                thumbnailUrl = searchResult.getSnippet().getThumbnails().getDefault().getUrl();
            }
        }

        return YoutubeSearchResponse.builder()
                .url("https://www.youtube.com/watch?v=" + searchResult.getId().getVideoId())
                .videoTitle(searchResult.getSnippet().getTitle())
                .thumbnailUrl(thumbnailUrl)
                .updatedAt(searchResult.getSnippet().getPublishedAt().toString())
                .build();
    }
}
