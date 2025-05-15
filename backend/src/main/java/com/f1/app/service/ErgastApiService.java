package com.f1.app.service;

import com.f1.app.dto.DriverDTO;
import com.f1.app.dto.RaceDTO;
import com.f1.app.dto.SeasonDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErgastApiService {
    
    @Value("${api.ergast.baseUrl}")
    private String baseUrl;
    
    private final RestTemplate restTemplate;
    
    public ResponseEntity<String> getSeasonList() {
        String url = baseUrl + "/f1/seasons.json";
        log.debug("Calling Ergast API: {}", url);
        return restTemplate.getForEntity(url, String.class);
    }
    
    public ResponseEntity<String> getDriverStandings(int year) {
        String url = baseUrl + "/f1/" + year + "/driverStandings.json";
        log.debug("Calling Ergast API: {}", url);
        return restTemplate.getForEntity(url, String.class);
    }
    
    public ResponseEntity<String> getRaceResults(int year) {
        String url = baseUrl + "/f1/" + year + "/results.json";
        log.debug("Calling Ergast API: {}", url);
        return restTemplate.getForEntity(url, String.class);
    }
    
    public ResponseEntity<String> getDriverInfo(String driverId) {
        String url = baseUrl + "/f1/drivers/" + driverId + ".json";
        log.debug("Calling Ergast API: {}", url);
        return restTemplate.getForEntity(url, String.class);
    }
    
    public ResponseEntity<String> getRaceWinners(int year) {
        String url = baseUrl + "/f1/" + year + "/results/1.json";
        log.debug("Calling Ergast API: {}", url);
        return restTemplate.getForEntity(url, String.class);
    }
} 