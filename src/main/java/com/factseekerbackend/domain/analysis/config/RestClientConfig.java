package com.factseekerbackend.domain.analysis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
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

    @Bean
    public RestClient naverRestClient(
            @Value("${naver.base-url}") String baseUrl,
            @Value("${naver.client.id}") String clientId,
            @Value("${naver.client.secret}") String clientSecret
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        JdkClientHttpRequestFactory rf = new JdkClientHttpRequestFactory(httpClient);
        rf.setReadTimeout(Duration.ofSeconds(5)); // 외부 API는 짧게 권장

        return RestClient.builder()
                .baseUrl(baseUrl) // https://openapi.naver.com
                .requestFactory(rf)
                .defaultHeader("X-Naver-Client-Id", clientId)
                .defaultHeader("X-Naver-Client-Secret", clientSecret)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
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
