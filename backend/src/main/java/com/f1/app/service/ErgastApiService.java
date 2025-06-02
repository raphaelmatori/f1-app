package com.f1.app.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.f1.app.dto.ErgastChampionResponse;
import com.f1.app.dto.ErgastRaceResponse;
import com.f1.app.dto.RaceDTO;
import com.f1.app.exception.ServiceException;
import com.f1.app.model.Champion;
import com.f1.app.model.Race;
import com.f1.app.model.RaceResult;
import com.f1.app.repository.ChampionRepository;
import com.f1.app.repository.RaceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErgastApiService {
    private static final int MAX_RETRIES = 5;
    private static final long INITIAL_RETRY_DELAY = 1000L;
    private final RestTemplate restTemplate;
    private final ChampionRepository championRepository;
    private final RaceRepository raceRepository;
    private final CacheService cacheService;

    @Value("${api.ergast.baseUrl}")
    private String baseUrl;

    @Retryable(
            value = { HttpClientErrorException.TooManyRequests.class },
            maxAttempts = MAX_RETRIES,
            backoff = @Backoff(delay = INITIAL_RETRY_DELAY, multiplier = 2, maxDelay = 15000)
    )
    public ResponseEntity<Champion> fetchWorldChampion(Integer year) {
        String url = String.format("%s/%d/driverstandings/1.json", baseUrl, year);
        log.info("Fetching world champion for year: {}", year);

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
    }

    @Retryable(
            value = { HttpClientErrorException.TooManyRequests.class },
            maxAttempts = MAX_RETRIES,
            backoff = @Backoff(delay = INITIAL_RETRY_DELAY, multiplier = 2, maxDelay = 15000)
    )
    public List<RaceDTO> fetchAndSaveRaces(Integer year, String baseUrl) {
        List<Race> allRaces = new ArrayList<>();
        int offset = 0;
        int total = 0;

        do {
            String url = String.format("%s/%d/results.json?limit=100&offset=%d", baseUrl, year, offset);
            log.info("Fetching races for year: {} (offset: {})", year, offset);

            ResponseEntity<ErgastRaceResponse> response = restTemplate.getForEntity(url, ErgastRaceResponse.class);
            if (response.getBody() == null || response.getBody().getMrData() == null) {
                throw new ServiceException(
                        "Failed to fetch races",
                        "RACES_FETCH_ERROR",
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                );
            }

            ErgastRaceResponse.MRData mrData = response.getBody().getMrData();
            if (mrData.getRaceTable() == null) {
                throw new ServiceException(
                        "Failed to fetch races",
                        "RACES_FETCH_ERROR",
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                );
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
                        newRace.getResults().forEach(race::addResult);
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

        try {
            saveRacesInDatabaseAsync(allRaces, year);
        } catch (Exception e) {
            log.error("Error saving races to database: {}", e.getMessage());
            throw new ServiceException(
                    "Failed to save races to database",
                    "RACES_SAVE_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    e
            );
        }

        // Convert to DTOs
        List<RaceDTO> raceDTOs = allRaces.stream()
                .map(RaceDTO::fromEntity)
                .collect(Collectors.toList());

        // Evict cache for this year before saving new data
        cacheService.evictRaceCache(year);

        return raceDTOs;

    }

    @Async
    public CompletableFuture<Void> saveRacesInDatabaseAsync(List<Race> races, Integer year) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<Race> saved = new ArrayList<>();
                for (Race race : races) {
                    Optional<Race> existingRace = raceRepository.findBySeasonAndRound(year, race.getRound());

                    if (existingRace.isPresent()) {
                        Race existing = existingRace.get();
                        // Only update if there are new results
                        if (race.getResults() != null && !race.getResults().isEmpty()) {
                            // Get new results that don't exist in the current race
                            List<RaceResult> newResults = race.getResults().stream()
                                .filter(newResult -> !existing.getResults().stream()
                                    .anyMatch(existingResult ->
                                        existingResult.getDriver().getDriverId().equals(newResult.getDriver().getDriverId())))
                                .collect(Collectors.toList());

                            // Only save if we actually have new results
                            if (!newResults.isEmpty()) {
                                // Add only new results to the existing race
                                newResults.forEach(existing::addResult);
                                saved.add(raceRepository.save(existing));
                            }
                        }
                    } else {
                        // This is a new race, save it
                        saved.add(raceRepository.save(race));
                    }
                }
                log.info("Processed {} races for year {}, {} were new or updated", races.size(), year, saved.size());
            } catch (Exception ex) {
                log.error("Failed to save races asynchronously for year {}: {}", year, ex.getMessage(), ex);
            }
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

    public Optional<Race> fetchLastRaceOfSeason(int year) {
        String url = String.format("%s/%d.json?limit=100", baseUrl, year);
        ResponseEntity<ErgastRaceResponse> response = restTemplate.getForEntity(url, ErgastRaceResponse.class);

        if (response.getBody() == null || response.getBody().getMrData() == null
                || response.getBody().getMrData().getRaceTable() == null
                || response.getBody().getMrData().getRaceTable().getRaces() == null
                || response.getBody().getMrData().getRaceTable().getRaces().isEmpty()) {
            return Optional.empty();
        }

        List<ErgastRaceResponse.RaceData> races = response.getBody().getMrData().getRaceTable().getRaces();
        return races.stream()
                .max(Comparator.comparingInt(race -> Integer.parseInt(race.getRound())))
                .map(this::mapToRace);
    }
}
