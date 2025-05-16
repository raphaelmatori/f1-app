package com.f1.app.service;

import com.f1.app.dto.ErgastResponse;
import com.f1.app.model.Champion;
import com.f1.app.repository.ChampionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErgastApiService {

    @Value("${ergast.api.baseUrl}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    private final ChampionRepository championRepository;

    public ResponseEntity<Champion> fetchWorldChampion(Integer year) {
        String url = String.format("%s/%d/driverstandings/1.json", baseUrl, year);
        log.info("Fetching world champion for year: {}", year);
        
        try {
            ErgastResponse response = restTemplate.getForObject(url, ErgastResponse.class);
            
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
                    
                    return ResponseEntity.ok(championRepository.save(champion));
                }
            }
            log.warn("No champion data found for year: {}", year);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching champion data for year {}: {}", year, e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}
