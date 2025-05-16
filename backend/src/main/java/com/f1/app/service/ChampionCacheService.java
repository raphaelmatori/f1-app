package com.f1.app.service;

import com.f1.app.model.Champion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChampionCacheService {

    private static final String CACHE_NAME = "champions";
    
    private final RedisCacheManager redisCacheManager;
    private final ErgastApiService ergastApiService;

    public ResponseEntity<Champion> getChampion(Integer year) {
        // Try Redis cache
        Optional<Champion> redisCacheResult = getFromRedisCache(year);
        if (redisCacheResult.isPresent()) {
            log.debug("Redis cache hit for year: {}", year);
            return ResponseEntity.ok(redisCacheResult.get());
        }

        // Get from API and update both caches
        ResponseEntity<Champion> apiResult = ergastApiService.fetchWorldChampion(year);
        if (apiResult.getStatusCode().is2xxSuccessful() && apiResult.getBody() != null) {
            Champion champion = apiResult.getBody();
            putInRedisCache(year, champion);
        }
        
        return apiResult;
    }

    private Optional<Champion> getFromRedisCache(Integer year) {
        return Optional.ofNullable(redisCacheManager.getCache(CACHE_NAME))
                .map(cache -> cache.get(year, Champion.class));
    }

    private void putInRedisCache(Integer year, Champion champion) {
        Optional.ofNullable(redisCacheManager.getCache(CACHE_NAME))
                .ifPresent(cache -> cache.put(year, champion));
    }
} 
