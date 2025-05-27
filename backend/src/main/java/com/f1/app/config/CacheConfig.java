package com.f1.app.config;

import com.f1.app.dto.RaceDTO;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EnableCaching
@Configuration
public class CacheConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.username:default}")
    private String redisUsername;

    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(30))
                .maximumSize(100));
        return cacheManager;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setPassword(redisPassword);
        config.setUsername(redisUsername);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        return mapper;
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory factory, ObjectMapper mapper) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Cache for RaceDTO
        JavaType raceListType = mapper.getTypeFactory().constructCollectionType(List.class, RaceDTO.class);
        Jackson2JsonRedisSerializer<?> raceSerializer = new Jackson2JsonRedisSerializer<>(mapper, raceListType);

        cacheConfigurations.put("races", RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(raceSerializer)));

        // Default fallback
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig();

        return RedisCacheManager.builder(factory)
                .withInitialCacheConfigurations(cacheConfigurations)
                .cacheDefaults(defaultConfig)
                .build();
    }
} 
