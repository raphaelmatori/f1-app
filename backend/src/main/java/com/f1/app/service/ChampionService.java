package com.f1.app.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.f1.app.dto.ChampionDTO;
import com.f1.app.exception.ServiceException;
import com.f1.app.model.Champion;
import com.f1.app.model.SeasonInfo;
import com.f1.app.repository.ChampionRepository;
import com.f1.app.repository.SeasonInfoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChampionService {

    private final ErgastApiService ergastApiService;
    private final ChampionRepository championRepository;
    private final SeasonInfoRepository seasonInfoRepository;
    private final RedisCacheManager redisCacheManager;
    private final CacheService cacheService;
    private static final String CACHE_NAME = "champions";

    @CacheEvict(value = "champions", key = "'currentYear'")
    public void evictCurrentYearCache() {
        log.info("Evicting current year champion cache");
        cacheService.evictChampionCache("currentYear");
    }

    @CacheEvict(value = "champions", key = "'allChampions'")
    public void evictAllChampionsCache() {
        log.info("Evicting all champions list cache");
        cacheService.evictChampionCache("allChampions");
    }

    @Async
    @Transactional
    public void initializeChampionData() {
        try {
            evictCurrentYearCache(); // Evict current year cache before update
            log.info("Starting champion data initialization...");
            int currentYear = java.time.Year.now().getValue();
            final int END_YEAR = 2005;

            // Get all years we already have in the database
            Set<Integer> existingYears = championRepository.findAll().stream()
                    .map(Champion::getYear)
                    .collect(Collectors.toSet());

            // Create a list of years we need to fetch, from current year down to 2005
            // Exclude current year as we'll handle it separately
            List<Integer> yearsToFetch = IntStream.rangeClosed(END_YEAR, currentYear - 1)
                    .boxed()
                    .filter(year -> !existingYears.contains(year))
                    .sorted((a, b) -> b.compareTo(a)) // Sort in descending order
                    .collect(Collectors.toList());

            log.info("Found {} past years that need to be fetched", yearsToFetch.size());

            // Always fetch current year first
            try {
                log.info("Fetching current year ({}) champion data...", currentYear);
                ResponseEntity<Champion> response = ergastApiService.fetchWorldChampion(currentYear);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Champion champion = response.getBody();
                    championRepository.save(champion);
                    log.debug("Saved current year champion data");
                }
                // Throttle requests
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error("Failed to fetch and save champion for current year {}: {}", currentYear, e.getMessage());
            }

            // Fetch and save missing champions for past years
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
        } catch (Exception e) {
            log.error("Error initializing champion data", e);
            throw new ServiceException(
                    "Failed to initialize champion data",
                    "CHAMPION_INIT_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Cacheable(value = "champions", key = "'allChampions'")
    public ResponseEntity<List<ChampionDTO>> getChampions() {
        try {
            int currentYear = java.time.Year.now().getValue();
            SeasonInfo currentSeasonInfo = seasonInfoRepository.findByYear(currentYear);
            
            List<ChampionDTO> champions = championRepository.findAll().stream()
                    .filter(champion -> {
                        // If it's current year, check availability
                        if (champion.getYear().equals(currentYear)) {
                            return currentSeasonInfo != null && currentSeasonInfo.isChampionAvailableForCurrentYear();
                        }
                        // For past years, always include
                        return true;
                    })
                    .map(ChampionDTO::fromEntity)
                    .collect(Collectors.toList());
            
            if (champions.isEmpty()) {
                throw new ServiceException(
                    "No champions found",
                    "NO_CHAMPIONS_FOUND",
                    HttpStatus.NOT_FOUND.value()
                );
            }
            
            return ResponseEntity.ok(champions);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching champions", e);
            throw new ServiceException(
                "Failed to fetch champions",
                "CHAMPIONS_FETCH_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Cacheable(value = "champions", key = "#year", unless = "#result.statusCode.is4xxClientError()")
    public ResponseEntity<ChampionDTO> getChampion(int year) {
        try {
            // For current year, check availability first
            if (year == java.time.Year.now().getValue()) {
                SeasonInfo seasonInfo = seasonInfoRepository.findByYear(year);
                if (seasonInfo == null || !seasonInfo.isChampionAvailableForCurrentYear()) {
                    throw new ServiceException(
                        String.format("Champion data for year %d is not yet available", year),
                        "CHAMPION_NOT_AVAILABLE",
                        HttpStatus.NOT_FOUND.value()
                    );
                }
            }

            Optional<Champion> existingChampion = championRepository.findByYear(year);
            if (existingChampion.isPresent()) {
                return ResponseEntity.ok(ChampionDTO.fromEntity(existingChampion.get()));
            }

            ResponseEntity<Champion> apiResponse = ergastApiService.fetchWorldChampion(year);
            if (apiResponse.getStatusCode() != HttpStatus.OK || apiResponse.getBody() == null) {
                throw new ServiceException(
                    String.format("No champion found for year %d", year),
                    "CHAMPION_NOT_FOUND",
                    HttpStatus.NOT_FOUND.value()
                );
            }

            Champion champion = apiResponse.getBody();
            try {
                championRepository.save(champion);
                // Evict the allChampions cache since we added a new champion
                evictAllChampionsCache();
            } catch (Exception e) {
                log.warn("Failed to save champion to database", e);
                // Continue execution as we still have the champion data
            }

            return ResponseEntity.ok(ChampionDTO.fromEntity(champion));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching champion for year {}", year, e);
            throw new ServiceException(
                "Failed to fetch champion data",
                "CHAMPION_FETCH_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
} 
