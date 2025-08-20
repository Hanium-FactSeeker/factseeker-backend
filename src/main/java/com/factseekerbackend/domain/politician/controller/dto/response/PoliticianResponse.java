package com.factseekerbackend.domain.politician.controller.dto.response;

import com.factseekerbackend.domain.politician.entity.Politician;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PoliticianResponse {

    private Long id;
    private String name;
    private String birthDate;
    private String party;
    private String position;
    private String region;
    private String facebookUrl;
    private String instagramUrl;
    private String xUrl;
    private String youtubeUrl;
    private String profileImageUrl;
    private boolean isActive;
    private LocalDateTime createdAt;

    public static PoliticianResponse from(Politician politician) {
        return PoliticianResponse.builder()
                .id(politician.getId())
                .name(politician.getName())
                .birthDate(politician.getBirthDate())
                .party(politician.getParty())
                .facebookUrl(politician.getFacebookUrl())
                .instagramUrl(politician.getInstagramUrl())
                .xUrl(politician.getXUrl())
                .youtubeUrl(politician.getYoutubeUrl())
                .profileImageUrl(politician.getProfileImageUrl())
                .isActive(politician.isActive())
                .createdAt(politician.getCreatedAt())
                .build();
    }
}
