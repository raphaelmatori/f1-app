package com.f1.app.service;

import com.f1.app.dto.ErgastRaceResponse;
import com.f1.app.dto.ErgastRaceResponse.RaceData;
import com.f1.app.model.Race;
import com.f1.app.model.RaceResult;
import com.f1.app.repository.RaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RaceService {

    private static final int PAGE_SIZE = 100;
    private static final String CACHE_NAME = "races";

    @Value("${ergast.api.baseUrl}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final RaceRepository raceRepository;
    private final RedisCacheManager redisCacheManager;

    public List<Race> getRacesByYear(Integer year) {
        // Try Redis cache first
        Optional<List<Race>> redisCacheResult = getFromRedisCache(year);
        if (redisCacheResult.isPresent()) {
            log.debug("Redis cache hit for year: {}", year);
            return redisCacheResult.get();
        }

        // Then check database
        List<Race> cachedRaces = raceRepository.findBySeason(year);
        if (!cachedRaces.isEmpty()) {
            log.debug("Found races for year {} in database", year);
            putInRedisCache(year, cachedRaces);
            return cachedRaces;
        }

        // If not in database, fetch from API with pagination
        log.info("Fetching races for year {} from API", year);
        List<Race> allRaces = new ArrayList<>();
        int offset = 0;
        int total;

        do {
            String url = String.format("%s/%d/results.json?limit=%d&offset=%d", baseUrl, year, PAGE_SIZE, offset);
            log.debug("Fetching page with offset {} from URL: {}", offset, url);
            
            ResponseEntity<ErgastRaceResponse> response = restTemplate.getForEntity(url, ErgastRaceResponse.class);
            if (response.getBody() == null || response.getBody().getMrData() == null) {
                break;
            }

            ErgastRaceResponse.MRData mrData = response.getBody().getMrData();
            total = Integer.parseInt(mrData.getTotal());

            if (mrData.getRaceTable() != null && mrData.getRaceTable().getRaces() != null) {
                List<Race> races = mrData.getRaceTable().getRaces()
                    .stream()
                    .map(this::mapToRace)
                    .collect(Collectors.toList());
                
                allRaces.addAll(races);
                log.debug("Fetched {} races from offset {}", races.size(), offset);
            }

            offset += PAGE_SIZE;
        } while (offset < total);

        if (!allRaces.isEmpty()) {
            // Save all fetched races to database
            allRaces = raceRepository.saveAll(allRaces);
            log.info("Saved total of {} races for year {}", allRaces.size(), year);
            // Cache in Redis
            putInRedisCache(year, allRaces);
        }

        return allRaces;
    }

    private Optional<List<Race>> getFromRedisCache(Integer year) {
        return Optional.ofNullable(redisCacheManager.getCache(CACHE_NAME))
                .map(cache -> cache.get(year))
                .map(cacheValue -> {
                    if (cacheValue != null && cacheValue.get() instanceof List<?>) {
                        List<?> list = (List<?>) cacheValue.get();
                        if (!list.isEmpty() && list.get(0) instanceof Race) {
                            return (List<Race>) list;
                        }
                    }
                    return null;
                });
    }

    private void putInRedisCache(Integer year, List<Race> races) {
        Optional.ofNullable(redisCacheManager.getCache(CACHE_NAME))
                .ifPresent(cache -> {
                    try {
                        cache.put(year, races);
                        log.debug("Successfully cached {} races for year {}", races.size(), year);
                    } catch (Exception e) {
                        log.error("Failed to cache races for year {}: {}", year, e.getMessage());
                    }
                });
    }

    private Race mapToRace(RaceData raceData) {
        Race.Circuit circuit = null;
        if (raceData.getCircuit() != null) {
            circuit = Race.Circuit.builder()
                .circuitId(raceData.getCircuit().getCircuitId())
                .circuitName(raceData.getCircuit().getCircuitName())
                .locality(raceData.getCircuit().getLocation() != null ? raceData.getCircuit().getLocation().getLocality() : null)
                .country(raceData.getCircuit().getLocation() != null ? raceData.getCircuit().getLocation().getCountry() : null)
                .build();
        }

        Race race = Race.builder()
            .season(Integer.parseInt(raceData.getSeason()))
            .round(Integer.parseInt(raceData.getRound()))
            .raceName(raceData.getRaceName())
            .date(raceData.getDate())
            .time(raceData.getTime())
            .circuit(circuit)
            .build();

        if (raceData.getResults() != null) {
            raceData.getResults().forEach(resultData -> {
                RaceResult result = RaceResult.builder()
                    .position(resultData.getPosition())
                    .points(resultData.getPoints())
                    .grid(resultData.getGrid())
                    .laps(resultData.getLaps())
                    .status(resultData.getStatus())
                    .driver(resultData.getDriver() != null ? RaceResult.Driver.builder()
                        .driverId(resultData.getDriver().getDriverId())
                        .code(resultData.getDriver().getCode())
                        .givenName(resultData.getDriver().getGivenName())
                        .familyName(resultData.getDriver().getFamilyName())
                        .nationality(resultData.getDriver().getNationality())
                        .build() : null)
                    .constructor(resultData.getConstructor() != null ? RaceResult.Constructor.builder()
                        .constructorId(resultData.getConstructor().getConstructorId())
                        .name(resultData.getConstructor().getName())
                        .nationality(resultData.getConstructor().getNationality())
                        .build() : null)
                    .time(resultData.getTime() != null ? RaceResult.RaceTime.builder()
                        .millis(resultData.getTime().getMillis())
                        .time(resultData.getTime().getTime())
                        .build() : null)
                    .build();
                race.addResult(result);
            });
        }

        return race;
    }
} 
