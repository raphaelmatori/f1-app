package com.f1.app.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Service;

import com.f1.app.dto.RaceDTO;
import com.f1.app.model.Race;
import com.f1.app.repository.RaceRepository;

@Service
public class RaceService {

    private static final Logger log = LoggerFactory.getLogger(RaceService.class);
    private static final String CACHE_NAME = "races";
    private final String baseUrl;
    private final RaceRepository raceRepository;
    private final RedisCacheManager redisCacheManager;
    private final ErgastApiService ergastApiService;
    private final CacheService cacheService;

    public RaceService(
            @Value("${api.ergast.baseUrl}") String baseUrl,
            RaceRepository raceRepository,
            RedisCacheManager redisCacheManager,
            ErgastApiService ergastApiService,
            CacheService cacheService) {
        this.baseUrl = baseUrl;
        this.raceRepository = raceRepository;
        this.redisCacheManager = redisCacheManager;
        this.ergastApiService = ergastApiService;
        this.cacheService = cacheService;
    }

    @Cacheable(value = "races", key = "#year")
    public List<RaceDTO> getRacesByYear(Integer year) {
        try {
            // Try Redis cache first
            Optional<List<RaceDTO>> redisCacheResult = getFromRedisCache(year);
            if (redisCacheResult.isPresent()) {
                log.debug("Redis cache hit for year: {}", year);
                return redisCacheResult.get();
            } else {
                log.debug("Redis cache not found for year: {}", year);
            }
        } catch (Exception e) {
            log.error("Error accessing Redis cache: {}", e.getMessage());
            // Continue with database lookup
        }

        // Then check database
        List<Race> cachedRaces = new ArrayList<>();
        List<RaceDTO> raceDTOs;
        try {
            cachedRaces = raceRepository.findBySeason(year);
        } catch (Exception e) {
            log.error("Database error while fetching races for year {}: {}", year, e.getMessage());
            // Fail gracefully and continue to API fallback
        }
        if (!cachedRaces.isEmpty()) {
            log.debug("Found races for year {} in database", year);

            raceDTOs = cachedRaces.stream()
                    .map(RaceDTO::fromEntity)
                    .collect(Collectors.toList());

            try {
                putInRedisCache(year, raceDTOs);
            } catch (Exception e) {
                log.error("Failed to update Redis cache: {}", e.getMessage());
                // Continue with database results
            }
            return raceDTOs;
        }

        // Use the retryable method from RaceApiService
        return ergastApiService.fetchAndSaveRaces(year, baseUrl);
    }

    private Optional<List<RaceDTO>> getFromRedisCache(Integer year) {
        try {
            return Optional.ofNullable(redisCacheManager.getCache(CACHE_NAME))
                    .map(cache -> cache.get(year))
                    .map(cacheValue -> {
                        if (cacheValue != null) {
                            @SuppressWarnings("unchecked")
                            List<RaceDTO> races = (List<RaceDTO>) cacheValue.get();
                            return races;
                        }
                        return null;
                    });
        } catch (Exception e) {
            log.error("Error accessing Redis cache for year {}: {}", year, e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private void putInRedisCache(Integer year, List<RaceDTO> races) {
        try {
            Optional.ofNullable(redisCacheManager.getCache(CACHE_NAME))
                    .ifPresent(cache -> {
                        cache.put(year, races);
                        log.debug("Successfully cached {} races for year {}", races.size(), year);
                    });
        } catch (Exception e) {
            log.error("Failed to cache races for year {}: {}", year, e.getMessage());
            // Continue without caching
        }
    }

    @CacheEvict(value = "races", key = "#year")
    public void evictRaceCache(Integer year) {
        log.info("Evicting race cache for year: {}", year);
        cacheService.evictRaceCache(year);
    }

    @CacheEvict(value = "races", allEntries = true)
    public void evictAllRaceCache() {
        log.info("Evicting all race caches");
        // Also evict from Redis cache explicitly
        Optional.ofNullable(redisCacheManager.getCache(CACHE_NAME))
                .ifPresent(cache -> {
                    cache.clear();
                    log.debug("Cleared all Redis caches for races");
                });
    }
} 
