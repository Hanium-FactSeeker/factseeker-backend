package com.factseekerbackend.domain.politician.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "politicians")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Politician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "birth_date")
    private String birthDate;

    @Column(length = 100)
    private String party;

    @Column(length = 100)
    private String position;

    @Column(length = 100)
    private String region;

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

    @Column(nullable = false)
    private boolean isActive = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public Politician(String name, String birthDate, String party, String position, String region, String facebookUrl, String instagramUrl, String xUrl, String youtubeUrl, String profileImageUrl) {
        this.name = name;
        this.birthDate = birthDate;
        this.party = party;
        this.position = position;
        this.region = region;
        this.facebookUrl = facebookUrl;
        this.instagramUrl = instagramUrl;
        this.xUrl = xUrl;
        this.youtubeUrl = youtubeUrl;
        this.profileImageUrl = profileImageUrl;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}
