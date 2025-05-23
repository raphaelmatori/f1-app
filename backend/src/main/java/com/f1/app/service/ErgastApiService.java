package com.f1.app.service;

import com.f1.app.dto.ErgastChampionResponse;
import com.f1.app.dto.ErgastRaceResponse;
import com.f1.app.dto.RaceDTO;
import com.f1.app.model.Champion;
import com.f1.app.model.Race;
import com.f1.app.model.RaceResult;
import com.f1.app.repository.ChampionRepository;
import com.f1.app.repository.RaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErgastApiService {
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY = 1000L;

    @Value("${api.ergast.baseUrl}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final ChampionRepository championRepository;
    private final RaceRepository raceRepository;
    private final RedisCacheManager redisCacheManager;

    @Retryable(
            value = { RestClientException.class },
            maxAttempts = MAX_RETRIES,
            backoff = @Backoff(delay = INITIAL_RETRY_DELAY, multiplier = 2)
    )
    public ResponseEntity<Champion> fetchWorldChampion(Integer year) {
        String url = String.format("%s/%d/driverstandings/1.json", baseUrl, year);
        log.info("Fetching world champion for year: {} (attempt {})", year, 1);

        try {
            ErgastChampionResponse response = restTemplate.getForObject(url, ErgastChampionResponse.class);
            if (response != null && response.getMrData() != null && response.getMrData().getStandingsTable() != null) {
                ErgastChampionResponse.StandingsList[] standingsLists = response.getMrData().getStandingsTable().getStandingsLists();
                if (standingsLists != null && standingsLists.length > 0) {
                    ErgastChampionResponse.DriverStanding[] driverStandings = standingsLists[0].getDriverStandings();
                    if (driverStandings != null && driverStandings.length > 0) {
                        ErgastChampionResponse.Driver driver = driverStandings[0].getDriver();
                        Champion champion = new Champion(
                                year,
                                driver.getDriverId(),
                                driver.getCode(),
                                driver.getGivenName(),
                                driver.getFamilyName(),
                                driver.getNationality(),
                                Float.parseFloat(driverStandings[0].getPoints()),
                                Integer.parseInt(driverStandings[0].getWins())
                        );

                        Champion savedChampion = championRepository.save(champion);
                        log.info("Successfully saved champion for year: {}", year);
                        return ResponseEntity.ok(savedChampion);
                    }
                }
            }
            log.warn("No champion data found for year: {}", year);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching champion for year {}: {}", year, e.getMessage());
            throw new RestClientException("Failed to fetch champion data", e);
        }
    }

    @Retryable(
            value = { RestClientException.class },
            maxAttempts = MAX_RETRIES,
            backoff = @Backoff(delay = INITIAL_RETRY_DELAY, multiplier = 2)
    )
    public List<RaceDTO> fetchAndSaveRaces(Integer year, String baseUrl) {
        List<Race> allRaces = new ArrayList<>();
        int offset = 0;
        int total = 0;

        try {
            do {
                String url = String.format("%s/%d/results.json?limit=100&offset=%d", baseUrl, year, offset);
                log.info("Fetching races for year: {} (offset: {})", year, offset);

                ResponseEntity<ErgastRaceResponse> response = restTemplate.getForEntity(url, ErgastRaceResponse.class);
                if (response.getBody() == null || response.getBody().getMrData() == null) {
                    throw new RuntimeException("Failed to fetch races");
                }

                ErgastRaceResponse.MRData mrData = response.getBody().getMrData();
                if (mrData.getRaceTable() == null) {
                    throw new RuntimeException("Failed to fetch races");
                }

                total = Integer.parseInt(mrData.getTotal());

                if (mrData.getRaceTable().getRaces() != null) {
                    List<Race> races = mrData.getRaceTable().getRaces()
                            .stream()
                            .map(this::mapToRace)
                            .filter(race -> race != null)
                            .collect(Collectors.toList());

                    // Merge races with existing ones if they exist
                    for (Race newRace : races) {
                        Optional<Race> existingRace = allRaces.stream()
                                .filter(race -> race.getRound().equals(newRace.getRound()))
                                .findFirst();

                        if (existingRace.isPresent()) {
                            Race race = existingRace.get();
                            // Add all results from new race to existing race
                            newRace.getResults().forEach(result -> {
                                race.addResult(result);
                            });
                        } else {
                            allRaces.add(newRace);
                        }
                    }
                }
                offset += 100;
            } while (offset < total);

            if (allRaces.isEmpty()) {
                log.warn("No races found for year {}", year);
                return new ArrayList<>();
            }

            // Save races in database asynchronously
            saveRacesInDatabaseAsync(allRaces, year);

            // Convert to DTOs and save in Redis cache asynchronously
            List<RaceDTO> raceDTOs = allRaces.stream()
                    .map(RaceDTO::fromEntity)
                    .collect(Collectors.toList());
            saveRacesInRedisAsync(year, raceDTOs);

            return raceDTOs;
        } catch (Exception e) {
            log.error("Error fetching races for year {}: {}", year, e.getMessage());
            throw new RuntimeException("Failed to fetch races", e);
        }
    }

    @Async
    public CompletableFuture<Void> saveRacesInRedisAsync(Integer year, List<RaceDTO> raceDTOs) {
        try {
            log.info("Caching races for year {} in Redis", year);
            Optional.ofNullable(redisCacheManager.getCache("races")).ifPresent(cache -> {
                cache.put(year, raceDTOs);
            });
            return CompletableFuture.runAsync(() -> {
                log.info("Successfully cached {} races for year {}", raceDTOs.size(), year);
            });
        } catch (Exception e) {
            log.error("Failed to cache races in Redis: {}", e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }

    @Async
    public CompletableFuture<Void> saveRacesInDatabaseAsync(List<Race> races, Integer year) {
        List<Race> saved = new ArrayList<>();
        for (Race race : races) {
            saved.add(raceRepository.save(race));
        }

        return CompletableFuture.runAsync(() -> {
            log.info("Saved {} races for year {}", races.size(), year);
        });
    }

    private Race mapToRace(ErgastRaceResponse.RaceData raceData) {
        if (raceData == null || raceData.getSeason() == null || raceData.getRound() == null) {
            log.warn("Invalid race data received: {}", raceData == null ? "null" : "missing season or round");
            return null;
        }

        Race race = Race.builder()
                .season(Integer.parseInt(raceData.getSeason()))
                .round(Integer.parseInt(raceData.getRound()))
                .raceName(raceData.getRaceName())
                .date(raceData.getDate())
                .time(raceData.getTime())
                .results(new ArrayList<>())
                .build();

        if (raceData.getCircuit() != null) {
            ErgastRaceResponse.LocationData location = raceData.getCircuit().getLocation();
            Race.Circuit circuit = Race.Circuit.builder()
                    .circuitId(raceData.getCircuit().getCircuitId())
                    .circuitName(raceData.getCircuit().getCircuitName())
                    .locality(location != null ? location.getLocality() : null)
                    .country(location != null ? location.getCountry() : null)
                    .build();
            race.setCircuit(circuit);
        }

        if (raceData.getResults() != null) {
            for (ErgastRaceResponse.ResultData resultData : raceData.getResults()) {
                if (resultData == null) continue;

                RaceResult raceResult = RaceResult.builder()
                        .position(resultData.getPosition())
                        .points(resultData.getPoints())
                        .grid(resultData.getGrid())
                        .laps(resultData.getLaps())
                        .status(resultData.getStatus())
                        .build();

                if (resultData.getDriver() != null) {
                    RaceResult.Driver driver = RaceResult.Driver.builder()
                            .driverId(resultData.getDriver().getDriverId())
                            .code(resultData.getDriver().getCode())
                            .givenName(resultData.getDriver().getGivenName())
                            .familyName(resultData.getDriver().getFamilyName())
                            .nationality(resultData.getDriver().getNationality())
                            .build();
                    raceResult.setDriver(driver);
                }

                if (resultData.getConstructor() != null) {
                    RaceResult.Constructor constructor = RaceResult.Constructor.builder()
                            .constructorId(resultData.getConstructor().getConstructorId())
                            .name(resultData.getConstructor().getName())
                            .nationality(resultData.getConstructor().getNationality())
                            .build();
                    raceResult.setConstructor(constructor);
                }

                if (resultData.getTime() != null) {
                    RaceResult.RaceTime time = RaceResult.RaceTime.builder()
                            .millis(resultData.getTime().getMillis())
                            .time(resultData.getTime().getTime())
                            .build();
                    raceResult.setTime(time);
                }

                raceResult.setRace(race);
                race.addResult(raceResult);
            }
        }

        return race;
    }
}
