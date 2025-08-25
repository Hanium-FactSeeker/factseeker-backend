package com.factseekerbackend.global.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("factCheckExecutor")
    public Executor factCheckExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);        // 기본 1개 스레드
        executor.setMaxPoolSize(5);         // 최대 5개 스레드
        executor.setQueueCapacity(25);      // 큐 용량도 줄임
        executor.setThreadNamePrefix("FactCheck-");
        executor.initialize();
        return executor;
    }

    @Bean("batchExecutor")
    public Executor batchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);        // 기본 1개 스레드
        executor.setMaxPoolSize(3);         // 최대 3개 스레드
        executor.setQueueCapacity(10);      // 큐 용량도 줄임
        executor.setThreadNamePrefix("Batch-");
        executor.initialize();
        return executor;
    }
}
