package com.factseekerbackend.domain.youtube.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
public class YoutubeConfig {

    @Bean
    public YouTube youTube() throws GeneralSecurityException, IOException {
        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                request -> {
                    // 보수적 타임아웃 설정: 스케줄러에서 오래 걸리는 요청 방지
                    request.setConnectTimeout(5_000); // 5s
                    request.setReadTimeout(8_000);    // 8s
                }
        ).setApplicationName("fact-seeker")
                .build();
    }
}
