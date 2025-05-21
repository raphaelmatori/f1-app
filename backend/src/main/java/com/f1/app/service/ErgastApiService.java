package com.f1.app.service;

import com.f1.app.dto.ErgastChampionResponse;
import com.f1.app.dto.ErgastRaceResponse;
import com.f1.app.model.Champion;
import com.f1.app.model.Race;
import com.f1.app.model.RaceResult;
import com.f1.app.repository.ChampionRepository;
import com.f1.app.repository.RaceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    @Cacheable(value = "champions", key = "#year")
    public ResponseEntity<Champion> fetchWorldChampion(Integer year) {
        String url = String.format("%s/%d/driverstandings/1.json", baseUrl, year);
        log.info("Fetching world champion for year: {} (attempt {})", year, 1);
        
        try {
            ErgastChampionResponse response = restTemplate.getForObject(url, ErgastChampionResponse.class);
            
            if (response != null && 
                response.getMrData() != null &&
                response.getMrData().getStandingsTable() != null &&
                response.getMrData().getStandingsTable().getStandingsLists() != null &&
                response.getMrData().getStandingsTable().getStandingsLists().length > 0) {
                
                var standingsList = response.getMrData().getStandingsTable().getStandingsLists()[0];
                if (standingsList.getDriverStandings() != null && 
                    standingsList.getDriverStandings().length > 0) {
                    
                    var driverStanding = standingsList.getDriverStandings()[0];
                    var driver = driverStanding.getDriver();
                
                    Champion champion = new Champion(
                        year,
                        driver.getDriverId(),
                        driver.getCode(),
                        driver.getGivenName(),
                        driver.getFamilyName(),
                        driver.getNationality(),
                        Float.parseFloat(driverStanding.getPoints()),
                        Integer.parseInt(driverStanding.getWins())
                    );
                    
                    try {
                        return ResponseEntity.ok(championRepository.save(champion));
                    } catch (Exception e) {
                        log.error("Failed to save champion to database: {}", e.getMessage());
                        return ResponseEntity.ok(champion);
                    }
                }
            }
            log.warn("No champion data found for year: {}", year);
            return ResponseEntity.notFound().build();
        } catch (RestClientException e) {
            log.error("API request failed for year {}: {}", year, e.getMessage());
            throw e; // Trigger retry
        } catch (Exception e) {
            log.error("Unexpected error while fetching champion for year {}: {}", year, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Retryable(
            value = { RestClientException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public List<Race> fetchAndSaveRaces(Integer year, String baseUrl) {
        log.info("Fetching races for year {} from API", year);
        List<Race> allRaces = new ArrayList<>();
        int offset = 0;
        int total = 0;

        try {
            do {
                String url = String.format("%s/%d/results.json?limit=%d&offset=%d", baseUrl, year, 100, offset);
                log.debug("Fetching page with offset {} from URL: {}", offset, url);
                try {
                    ResponseEntity<ErgastRaceResponse> response = restTemplate.getForEntity(url, ErgastRaceResponse.class);
                    if (response.getBody() == null || response.getBody().getMrData() == null) {
                        log.warn("Received empty response from API for year {} at offset {}", year, offset);
                        break;
                    }
                    ErgastRaceResponse.MRData mrData = response.getBody().getMrData();
                    total = Integer.parseInt(mrData.getTotal());
                    if (mrData.getRaceTable() != null && mrData.getRaceTable().getRaces() != null) {
                        List<Race> races = mrData.getRaceTable().getRaces()
                                .stream()
                                .map(this::mapToRace)
                                .filter(race -> race != null)
                                .collect(Collectors.toList());
                        if (!races.isEmpty()) {
                            // Handle duplicate races by appending results
                            for (Race newRace : races) {
                                Optional<Race> existingRace = allRaces.stream()
                                        .filter(race -> race.getCircuit() != null && 
                                                      newRace.getCircuit() != null && 
                                                      race.getCircuit().getCircuitId().equals(newRace.getCircuit().getCircuitId()))
                                        .findFirst();
                                
                                if (existingRace.isPresent()) {
                                    // Append results to existing race
                                    existingRace.get().getResults().addAll(newRace.getResults());
                                    log.debug("Appended results to existing race at circuit: {}", newRace.getCircuit().getCircuitId());
                                } else {
                                    // Add new race
                                    allRaces.add(newRace);
                                    log.debug("Added new race for circuit: {}", newRace.getCircuit().getCircuitId());
                                }
                            }
                        }
                    }
                } catch (RestClientException e) {
                    if (e.getMessage().contains("Read timeout")) {
                        log.error("Read timeout occurred while fetching races for year {} at offset {}: {}", year, offset, e.getMessage());
                        throw e;
                    } else {
                        log.error("API request failed for year {} at offset {}: {}", year, offset, e.getMessage());
                        throw e;
                    }
                }
                offset += 100;
            } while (offset < total);

            if (!allRaces.isEmpty()) {
                try {
                    for (Race race : allRaces) {
                        raceRepository.save(race);
                    }
                    log.info("Saved total of {} races for year {}", allRaces.size(), year);
                    try {
                        Optional.ofNullable(redisCacheManager.getCache("races"))
                                .ifPresent(cache -> {
                                    cache.put(year, allRaces);
                                    log.debug("Successfully cached {} races for year {}", allRaces.size(), year);
                                });
                    } catch (Exception e) {
                        log.error("Failed to update Redis cache: {}", e.getMessage());
                    }
                } catch (Exception e) {
                    log.error("Failed to save races to database: {}", e.getMessage());
                    return allRaces;
                }
            } else {
                log.warn("No races found for year {}", year);
            }
            return allRaces;
        } catch (RestClientException e) {
            if (e.getMessage().contains("Read timeout")) {
                log.error("All retry attempts failed due to read timeout for year {}: {}", year, e.getMessage());
            } else {
                log.error("API request failed for year {}: {}", year, e.getMessage());
            }
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while fetching races for year {}: {}", year, e.getMessage());
            return new ArrayList<>();
        }
    }

    private Race mapToRace(ErgastRaceResponse.RaceData raceData) {
        if (raceData == null) {
            log.warn("Invalid race data received: null");
            return null;
        }
        if (raceData.getSeason() == null || raceData.getRound() == null) {
            log.warn("Invalid race data received: missing season or round");
            return null;
        }
        Race.Circuit circuit = null;
        ErgastRaceResponse.CircuitData circuitData = raceData.getCircuit();
        if (circuitData != null) {
            ErgastRaceResponse.LocationData location = circuitData.getLocation();
            circuit = Race.Circuit.builder()
                    .circuitId(circuitData.getCircuitId())
                    .circuitName(circuitData.getCircuitName())
                    .locality(location != null ? location.getLocality() : null)
                    .country(location != null ? location.getCountry() : null)
                    .build();
        }
        Race race = Race.builder()
                .season(Integer.parseInt(raceData.getSeason()))
                .round(Integer.parseInt(raceData.getRound()))
                .raceName(raceData.getRaceName())
                .date(raceData.getDate())
                .time(raceData.getTime())
                .circuit(circuit)
                .results(new ArrayList<>())
                .build();
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
                ErgastRaceResponse.DriverData driverData = resultData.getDriver();
                if (driverData != null) {
                    raceResult.setDriver(RaceResult.Driver.builder()
                            .driverId(driverData.getDriverId())
                            .code(driverData.getCode())
                            .givenName(driverData.getGivenName())
                            .familyName(driverData.getFamilyName())
                            .nationality(driverData.getNationality())
                            .build());
                }
                ErgastRaceResponse.ConstructorData constructorData = resultData.getConstructor();
                if (constructorData != null) {
                    raceResult.setConstructor(RaceResult.Constructor.builder()
                            .constructorId(constructorData.getConstructorId())
                            .name(constructorData.getName())
                            .nationality(constructorData.getNationality())
                            .build());
                }
                ErgastRaceResponse.TimeData timeData = resultData.getTime();
                if (timeData != null) {
                    raceResult.setTime(RaceResult.RaceTime.builder()
                            .millis(timeData.getMillis())
                            .time(timeData.getTime())
                            .build());
                }
                race.addResult(raceResult);
            }
        }
        return race;
    }
}
