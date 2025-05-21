package com.f1.app.service;

import com.f1.app.model.Champion;
import com.f1.app.repository.ChampionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChampionService {

    private static final String CACHE_NAME = "champions";
    private static final int THREAD_POOL_SIZE = 5;
    
    private final RedisCacheManager redisCacheManager;
    private final ErgastApiService ergastApiService;
    private final ChampionRepository championRepository;

    @PostConstruct
    public void init() {
        // Trigger the async initialization
        initializeChampionData();
    }

    @Async
    @Scheduled(initialDelay = 0)
    @Transactional
    public void initializeChampionData() {
        log.info("Starting champion data initialization...");
        int currentYear = java.time.Year.now().getValue();
        final int END_YEAR = 2005;

        // Get all years we already have in the database
        Set<Integer> existingYears = championRepository.findAll().stream()
                .map(Champion::getYear)
                .collect(Collectors.toSet());

        // Create a list of years we need to fetch, from current year down to 2005
        List<Integer> yearsToFetch = IntStream.rangeClosed(END_YEAR, currentYear)
                .boxed()
                .filter(year -> !existingYears.contains(year))
                .sorted((a, b) -> b.compareTo(a)) // Sort in descending order
                .collect(Collectors.toList());

        log.info("Found {} years that need to be fetched", yearsToFetch.size());

        // Fetch and save missing champions
        for (Integer year : yearsToFetch) {
            try {
                ResponseEntity<Champion> response = ergastApiService.fetchWorldChampion(year);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Champion champion = response.getBody();
                    championRepository.save(champion);
                    log.debug("Saved champion data for year: {}", year);
                }
                // Throttle requests to 1 per second
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error("Failed to fetch and save champion for year {}: {}", year, e.getMessage());
            }
        }
        
        log.info("Champion data initialization completed");
    }

    public ResponseEntity<List<Champion>> getChampions() {
        List<Champion> champions = championRepository.findAll();
        return ResponseEntity.ok(champions);
    }

    public ResponseEntity<Champion> getChampion(Integer year) {
        Optional<Champion> champion = championRepository.findByYear(year);
        return champion.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    // If not in database, fetch from API and save
                    ResponseEntity<Champion> apiResult = ergastApiService.fetchWorldChampion(year);
                    if (apiResult.getStatusCode().is2xxSuccessful() && apiResult.getBody() != null) {
                        Champion newChampion = apiResult.getBody();
                        championRepository.save(newChampion);
                    }
                    return apiResult;
                });
    }
} 
