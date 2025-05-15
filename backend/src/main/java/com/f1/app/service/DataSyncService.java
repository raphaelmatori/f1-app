package com.f1.app.service;

import com.f1.app.dto.ergast.ErgastResponse;
import com.f1.app.model.Driver;
import com.f1.app.model.Race;
import com.f1.app.model.Season;
import com.f1.app.repository.DriverRepository;
import com.f1.app.repository.RaceRepository;
import com.f1.app.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSyncService {
    
    private final ErgastApiService ergastApiService;
    private final DriverRepository driverRepository;
    private final RaceRepository raceRepository;
    private final SeasonRepository seasonRepository;
    
    @Scheduled(cron = "0 0 0 * * *") // Run at midnight every day
    @Transactional
    public void syncData() {
        log.info("Starting data synchronization");
        try {
            syncDrivers();
            syncRaces();
            syncChampions();
            log.info("Data synchronization completed successfully");
        } catch (Exception e) {
            log.error("Error during data synchronization: {}", e.getMessage(), e);
        }
    }
    
    private void syncDrivers() {
        ErgastResponse response = ergastApiService.getDriverInfo("");
        if (response != null && response.getMrData().getDriverTable() != null) {
            for (ErgastResponse.Driver apiDriver : response.getMrData().getDriverTable().getDrivers()) {
                saveDriver(apiDriver);
            }
        }
    }
    
    private void syncRaces() {
        int currentYear = LocalDateTime.now().getYear();
        for (int year = currentYear; year >= 1950; year--) {
            ErgastResponse response = ergastApiService.getRaceResults(year);
            if (response != null && response.getMrData().getRaceTable() != null) {
                for (ErgastResponse.Race apiRace : response.getMrData().getRaceTable().getRaces()) {
                    saveRace(apiRace);
                }
            }
        }
    }
    
    private void syncChampions() {
        int currentYear = LocalDateTime.now().getYear();
        for (int year = currentYear; year >= 1950; year--) {
            ErgastResponse response = ergastApiService.getDriverStandings(year);
            if (response != null && response.getMrData().getStandingsTable() != null) {
                for (ErgastResponse.StandingsList standings : response.getMrData().getStandingsTable().getStandingsLists()) {
                    if (standings.getDriverStandings() != null && standings.getDriverStandings().length > 0) {
                        saveChampion(year, standings.getDriverStandings()[0]);
                    }
                }
            }
        }
    }
    
    private void saveDriver(ErgastResponse.Driver apiDriver) {
        Optional<Driver> existingDriver = driverRepository.findByDriverId(apiDriver.getDriverId());
        
        Driver driver = existingDriver.orElse(new Driver());
        driver.setDriverId(apiDriver.getDriverId());
        driver.setCode(apiDriver.getCode());
        driver.setFirstName(apiDriver.getGivenName());
        driver.setLastName(apiDriver.getFamilyName());
        driver.setDateOfBirth(apiDriver.getDateOfBirth());
        driver.setNationality(apiDriver.getNationality());
        driver.setUrl(apiDriver.getUrl());
        driver.setPermanentNumber(apiDriver.getPermanentNumber());
        
        driverRepository.save(driver);
    }
    
    private void saveRace(ErgastResponse.Race apiRace) {
        Race race = new Race();
        race.setSeason(Integer.parseInt(apiRace.getSeason()));
        race.setRound(Integer.parseInt(apiRace.getRound()));
        race.setRaceName(apiRace.getRaceName());
        race.setCircuitId(apiRace.getCircuit().getCircuitId());
        race.setCircuitName(apiRace.getCircuit().getCircuitName());
        race.setLocality(apiRace.getCircuit().getLocation().getLocality());
        race.setCountry(apiRace.getCircuit().getLocation().getCountry());
        
        // Parse date and time
        String dateStr = apiRace.getDate();
        String timeStr = apiRace.getTime() != null ? apiRace.getTime() : "00:00:00Z";
        LocalDateTime raceDateTime = LocalDateTime.parse(dateStr + "T" + timeStr.replace("Z", ""),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        race.setDate(raceDateTime);
        
        race.setUrl(apiRace.getUrl());
        
        // Set winner if available
        if (apiRace.getResults() != null && apiRace.getResults().length > 0) {
            ErgastResponse.Results winner = apiRace.getResults()[0];
            Optional<Driver> winningDriver = driverRepository.findByDriverId(winner.getDriver().getDriverId());
            winningDriver.ifPresent(race::setWinner);
            
            if (winner.getTime() != null) {
                race.setWinnerTime(winner.getTime().getTime());
            }
            race.setLaps(Integer.parseInt(winner.getLaps()));
        }
        
        raceRepository.save(race);
    }
    
    private void saveChampion(int year, ErgastResponse.DriverStanding championStanding) {
        Optional<Driver> champion = driverRepository.findByDriverId(championStanding.getDriver().getDriverId());
        if (champion.isPresent()) {
            Season season = seasonRepository.findById(year).orElse(new Season());
            season.setYear(year);
            season.setChampion(champion.get());
            season.setTotalPoints(Integer.parseInt(championStanding.getPoints()));
            season.setTotalWins(Integer.parseInt(championStanding.getWins()));
            
            // Additional statistics can be calculated here
            List<Race> seasonRaces = raceRepository.findBySeasonOrderByRoundAsc(year);
            season.setTotalRaces(seasonRaces.size());
            
            seasonRepository.save(season);
        }
    }
} 