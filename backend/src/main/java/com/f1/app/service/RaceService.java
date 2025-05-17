package com.f1.app.service;

import com.f1.app.dto.ErgastRaceResponse;
import com.f1.app.dto.ErgastRaceResponse.RaceData;
import com.f1.app.model.Race;
import com.f1.app.model.RaceResult;
import com.f1.app.repository.RaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RaceService {

    private static final int PAGE_SIZE = 100;

    @Value("${ergast.api.baseUrl}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final RaceRepository raceRepository;

    @Cacheable(value = "races", key = "#year")
    public List<Race> getRacesByYear(Integer year) {
        // First check if we have it in the database
        List<Race> cachedRaces = raceRepository.findBySeason(year);
        if (!cachedRaces.isEmpty()) {
            log.debug("Found races for year {} in database", year);
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
        }

        return allRaces;
    }

    private Race mapToRace(RaceData raceData) {
        Race race = new Race();
        race.setSeason(Integer.parseInt(raceData.getSeason()));
        race.setRound(Integer.parseInt(raceData.getRound()));
        race.setRaceName(raceData.getRaceName());
        race.setDate(raceData.getDate());
        race.setTime(raceData.getTime());

        // Map Circuit
        if (raceData.getCircuit() != null) {
            Race.Circuit circuit = new Race.Circuit();
            circuit.setCircuitId(raceData.getCircuit().getCircuitId());
            circuit.setCircuitName(raceData.getCircuit().getCircuitName());
            if (raceData.getCircuit().getLocation() != null) {
                circuit.setLocality(raceData.getCircuit().getLocation().getLocality());
                circuit.setCountry(raceData.getCircuit().getLocation().getCountry());
            }
            race.setCircuit(circuit);
        }

        // Map Results
        if (raceData.getResults() != null) {
            race.setResults(raceData.getResults().stream()
                .map(resultData -> {
                    RaceResult result = new RaceResult();
                    result.setPosition(resultData.getPosition());
                    result.setPoints(resultData.getPoints());
                    result.setGrid(resultData.getGrid());
                    result.setLaps(resultData.getLaps());
                    result.setStatus(resultData.getStatus());

                    // Map Driver
                    if (resultData.getDriver() != null) {
                        RaceResult.Driver driver = new RaceResult.Driver();
                        driver.setDriverId(resultData.getDriver().getDriverId());
                        driver.setCode(resultData.getDriver().getCode());
                        driver.setGivenName(resultData.getDriver().getGivenName());
                        driver.setFamilyName(resultData.getDriver().getFamilyName());
                        driver.setNationality(resultData.getDriver().getNationality());
                        result.setDriver(driver);
                    }

                    // Map Constructor
                    if (resultData.getConstructor() != null) {
                        RaceResult.Constructor constructor = new RaceResult.Constructor();
                        constructor.setConstructorId(resultData.getConstructor().getConstructorId());
                        constructor.setName(resultData.getConstructor().getName());
                        constructor.setNationality(resultData.getConstructor().getNationality());
                        result.setConstructor(constructor);
                    }

                    // Map Time
                    if (resultData.getTime() != null) {
                        RaceResult.RaceTime time = new RaceResult.RaceTime();
                        time.setMillis(resultData.getTime().getMillis());
                        time.setTime(resultData.getTime().getTime());
                        result.setTime(time);
                    }

                    return result;
                })
                .collect(Collectors.toList()));
        }

        return race;
    }
} 
