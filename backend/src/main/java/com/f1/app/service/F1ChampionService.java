package com.f1.app.service;

import com.f1.app.dto.DriverDTO;
import com.f1.app.dto.RaceDTO;
import com.f1.app.dto.SeasonDTO;
import com.f1.app.model.Driver;
import com.f1.app.model.Race;
import com.f1.app.model.Season;
import com.f1.app.repository.DriverRepository;
import com.f1.app.repository.RaceRepository;
import com.f1.app.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class F1ChampionService {
    
    private final DriverRepository driverRepository;
    private final RaceRepository raceRepository;
    private final SeasonRepository seasonRepository;
    private final ErgastApiService ergastApiService;
    
    @Transactional(readOnly = true)
    public List<SeasonDTO> getAllSeasons() {
        return seasonRepository.findAllByOrderByYearDesc().stream()
            .map(this::convertToSeasonDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Optional<SeasonDTO> getSeasonByYear(Integer year) {
        return seasonRepository.findById(year)
            .map(this::convertToSeasonDTO);
    }
    
    @Transactional(readOnly = true)
    public List<RaceDTO> getRacesBySeason(Integer year) {
        return raceRepository.findBySeasonOrderByRoundAsc(year).stream()
            .map(this::convertToRaceDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<DriverDTO> getWorldChampions() {
        List<Season> seasons = seasonRepository.findAllByOrderByYearDesc();
        return seasons.stream()
            .map(Season::getChampion)
            .distinct()
            .map(this::convertToDriverDTO)
            .collect(Collectors.toList());
    }
    
    private SeasonDTO convertToSeasonDTO(Season season) {
        SeasonDTO dto = new SeasonDTO();
        dto.setYear(season.getYear());
        dto.setChampion(convertToDriverDTO(season.getChampion()));
        dto.setTotalPoints(season.getTotalPoints());
        dto.setTotalRaces(season.getTotalRaces());
        dto.setTotalWins(season.getTotalWins());
        dto.setTotalPodiums(season.getTotalPodiums());
        dto.setNotes(season.getNotes());
        dto.setUrl(season.getUrl());
        return dto;
    }
    
    private RaceDTO convertToRaceDTO(Race race) {
        RaceDTO dto = new RaceDTO();
        dto.setId(race.getId());
        dto.setSeason(race.getSeason());
        dto.setRound(race.getRound());
        dto.setRaceName(race.getRaceName());
        dto.setCircuitId(race.getCircuitId());
        dto.setCircuitName(race.getCircuitName());
        dto.setLocality(race.getLocality());
        dto.setCountry(race.getCountry());
        dto.setDate(race.getDate());
        dto.setUrl(race.getUrl());
        dto.setWinner(race.getWinner() != null ? convertToDriverDTO(race.getWinner()) : null);
        dto.setWinnerTime(race.getWinnerTime());
        dto.setLaps(race.getLaps());
        dto.setNotes(race.getNotes());
        return dto;
    }
    
    private DriverDTO convertToDriverDTO(Driver driver) {
        DriverDTO dto = new DriverDTO();
        dto.setDriverId(driver.getDriverId());
        dto.setCode(driver.getCode());
        dto.setFirstName(driver.getFirstName());
        dto.setLastName(driver.getLastName());
        dto.setDateOfBirth(driver.getDateOfBirth());
        dto.setNationality(driver.getNationality());
        dto.setUrl(driver.getUrl());
        dto.setPermanentNumber(driver.getPermanentNumber());
        
        // Calculate statistics
        List<Season> championshipSeasons = seasonRepository.findByChampion_DriverId(driver.getDriverId());
        dto.setChampionships(championshipSeasons.size());
        
        List<Race> raceWins = raceRepository.findBySeasonAndWinner_DriverId(null, driver.getDriverId());
        dto.setTotalRaceWins(raceWins.size());
        
        return dto;
    }
} 
