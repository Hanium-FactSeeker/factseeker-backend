package com.factseekerbackend.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Value("${spring.data.redis.host}")
  private String redisHost;

  @Value("${spring.data.redis.port}")
  private int redisPort;

  @Value("${spring.data.redis.password}")
  private String redisPassword;

  @Value("${spring.data.redis.database}")
  private int database;

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
    redisConfig.setHostName(redisHost);
    redisConfig.setPort(redisPort);
    redisConfig.setDatabase(database);

    if (redisPassword != null && !redisPassword.trim().isEmpty()) {
      redisConfig.setPassword(redisPassword);
    }

    return new LettuceConnectionFactory(redisConfig);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(connectionFactory);

    // Key 직렬화
    redisTemplate.setKeySerializer(new StringRedisSerializer());

    // Value 직렬화: JSON 포맷 사용
    redisTemplate.setValueSerializer(jackson2JsonRedisSerializer());
    redisTemplate.setHashKeySerializer(new StringRedisSerializer());
    redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer());

    return redisTemplate;
  }

  private RedisSerializer<Object> jackson2JsonRedisSerializer() {
    PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator
        .builder()
        .allowIfBaseType(Object.class)
        .build();

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

    return new GenericJackson2JsonRedisSerializer(objectMapper);
  }
}
