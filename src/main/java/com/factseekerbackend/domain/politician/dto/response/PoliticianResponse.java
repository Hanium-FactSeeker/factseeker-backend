package com.factseekerbackend.domain.politician.dto.response;

import com.factseekerbackend.domain.politician.entity.Politician;

import java.math.BigDecimal;

public record PoliticianResponse(
        Long id,
        String name,
        String birthDate, // 문자열로 내려주면 프론트가 다루기 편함
        String party,
        String facebookUrl,
        String instagramUrl,
        String xUrl,
        String youtubeUrl,
        String profileImageUrl
) {
    public static PoliticianResponse from(Politician p) {
        return new PoliticianResponse(
                p.getId(),
                p.getName(),
                p.getBirthDate(),
                p.getParty(),
                p.getFacebookUrl(),
                p.getInstagramUrl(),
                p.getXUrl(),
                p.getYoutubeUrl(),
                p.getProfileImageUrl()
        );
    }
}