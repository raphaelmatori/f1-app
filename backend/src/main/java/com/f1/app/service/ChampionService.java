package com.f1.app.service;

import com.f1.app.exception.ServiceException;
import com.f1.app.model.Champion;
import com.f1.app.repository.ChampionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChampionService {

    private final ErgastApiService ergastApiService;
    private final ChampionRepository championRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void init() {
        // Trigger the async initialization
        initializeChampionData();
    }

    @Async
    @Scheduled(initialDelay = 0)
    @Transactional
    public void initializeChampionData() {
        try {
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
        } catch (Exception e) {
            log.error("Error initializing champion data", e);
            throw new ServiceException(
                "Failed to initialize champion data",
                "CHAMPION_INIT_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    public ResponseEntity<List<Champion>> getChampions() {
        try {
            List<Champion> champions = championRepository.findAll();
            return ResponseEntity.ok(champions);
        } catch (Exception e) {
            log.error("Error fetching champions", e);
            throw new ServiceException(
                "Failed to fetch champions",
                "CHAMPIONS_FETCH_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    public ResponseEntity<Champion> getChampion(int year) {
        try {
            Optional<Champion> existingChampion = championRepository.findByYear(year);
            if (existingChampion.isPresent()) {
                return ResponseEntity.ok(existingChampion.get());
            }

            ResponseEntity<Champion> apiResponse = ergastApiService.fetchWorldChampion(year);
            if (apiResponse.getStatusCode() != HttpStatus.OK || apiResponse.getBody() == null) {
                throw new ServiceException(
                    "Failed to fetch champion data from external API",
                    "CHAMPION_API_ERROR",
                    HttpStatus.SERVICE_UNAVAILABLE.value()
                );
            }

            Champion champion = apiResponse.getBody();
            try {
                championRepository.save(champion);
            } catch (Exception e) {
                log.warn("Failed to save champion to database", e);
                // Continue execution as we still have the champion data
            }

            return ResponseEntity.ok(champion);
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
