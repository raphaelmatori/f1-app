package com.f1.app.service;

import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {
    private static final String CHAMPIONS_CACHE = "champions";
    private static final String RACES_CACHE = "races";
    
    private final RedisCacheManager redisCacheManager;

    public void evictChampionCache(String key) {
        log.debug("Evicting champion cache for key: {}", key);
        Cache cache = redisCacheManager.getCache(CHAMPIONS_CACHE);
        if (cache != null) {
            cache.evict(key);
        }
    }

    public void evictRaceCache(Integer year) {
        log.debug("Evicting race cache for year: {}", year);
        Cache cache = redisCacheManager.getCache(RACES_CACHE);
        if (cache != null) {
            cache.evict(year.toString());
        }
    }
} 