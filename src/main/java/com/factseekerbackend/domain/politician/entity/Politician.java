package com.factseekerbackend.domain.politician.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "politicians",
        catalog = "politicians" // 필요 시 서버에서는 factseeker_ingest 로 변경
)
public class Politician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_kr", nullable = false, length = 100)
    private String nameKr;

    @Column(name = "birth_date")
    private String birthDate;

    @Column(name = "party", length = 100)
    private String party;

    @Column(name = "facebook_url", length = 512)
    private String facebookUrl;

    @Column(name = "instagram_url", length = 512)
    private String instagramUrl;

    @Column(name = "x_url", length = 512)
    private String xUrl;

    @Column(name = "youtube_url", length = 512)
    private String youtubeUrl;

    @Column(name = "profile_image_url", length = 512)
    private String profileImageUrl;

    @Column(name = "gpt_trust_score", precision = 5, scale = 2)
    private java.math.BigDecimal gptTrustScore;

    @Column(name = "claude_trust_score", precision = 5, scale = 2)
    private java.math.BigDecimal claudeTrustScore;

    @Column(name = "gemini_trust_score", precision = 5, scale = 2)
    private java.math.BigDecimal geminiTrustScore;
}