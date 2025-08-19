package com.factseekerbackend;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@OpenAPIDefinition(
    info = @Info(
        title = "FactSeeker Backend API",
        version = "1.0.0",
        description = "정치인 신뢰도 분석 및 팩트체크 백엔드 API"
    )
)
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
@SpringBootApplication
public class FactseekerBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(FactseekerBackendApplication.class, args);
  }

}
