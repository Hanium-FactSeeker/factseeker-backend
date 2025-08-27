package com.factseekerbackend.domain.apify.entity;

import com.factseekerbackend.domain.politician.entity.Politician;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "politician_sns_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor

@Builder
public class PoliticianSnsPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // politician_id 외래키
    @JoinColumn(name = "politician_id", nullable = false)
    private Politician politician;

    @Column(columnDefinition = "TEXT")
    private String postText;

    private LocalDateTime postDate;

    private Double trustScore; // AI 판별 후 신뢰도 점수 (0~100)
}
