package com.factseekerbackend.domain.analysis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executor;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient fastApiClient(
            @Value("${fastapi.base-url}") String baseUrl
    ) {
        String finalBaseUrl = baseUrl.startsWith("http") ? baseUrl : "http://" + baseUrl;

        // JDK HttpClient 기반 타임아웃
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        JdkClientHttpRequestFactory rf = new JdkClientHttpRequestFactory(httpClient);
        rf.setReadTimeout(Duration.ofSeconds(360));

        return RestClient.builder()
                .baseUrl(finalBaseUrl)
                .requestFactory(rf)
                .build();
    }

    @Primary
    @Bean(name = "factApiExecutor")
    public Executor factApiExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(3);
        ex.setMaxPoolSize(3);
        ex.setQueueCapacity(20);
        ex.setThreadNamePrefix("factapi-");
        ex.initialize();
        return ex;
    }
}
