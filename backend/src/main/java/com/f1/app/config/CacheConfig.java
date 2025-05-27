package com.f1.app.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.f1.app.dto.ChampionDTO;
import com.f1.app.dto.RaceDTO;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;

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

    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

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

        // Configure races cache
        JavaType raceListType = mapper.getTypeFactory().constructCollectionType(List.class, RaceDTO.class);
        Jackson2JsonRedisSerializer<?> raceSerializer = new Jackson2JsonRedisSerializer<>(mapper, raceListType);
        cacheConfigurations.put("races", RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(raceSerializer)));

        // Configure champions cache
        JavaType championListType = mapper.getTypeFactory().constructCollectionType(List.class, ChampionDTO.class);
        Jackson2JsonRedisSerializer<?> championSerializer = new Jackson2JsonRedisSerializer<>(mapper, championListType);
        cacheConfigurations.put("champions", RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(championSerializer)));

        // Default configuration for other caches
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL);

        return RedisCacheManager.builder(factory)
                .withInitialCacheConfigurations(cacheConfigurations)
                .cacheDefaults(defaultConfig)
                .build();
    }
} 
