package com.f1.app.service;

import com.f1.app.dto.ergast.ErgastResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErgastApiService {
    
    @Value("${api.ergast.baseUrl}")
    private String baseUrl;
    
    private final RestTemplate restTemplate;
    
    public ErgastResponse getSeasonList() {
        String url = baseUrl + "/f1/seasons.json";
        log.debug("Calling Ergast API: {}", url);
        try {
            ResponseEntity<ErgastResponse> response = restTemplate.getForEntity(url, ErgastResponse.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error calling Ergast API: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch season list from Ergast API", e);
        }
    }
    
    public ErgastResponse getDriverStandings(int year) {
        String url = baseUrl + "/f1/" + year + "/driverStandings.json";
        log.debug("Calling Ergast API: {}", url);
        try {
            ResponseEntity<ErgastResponse> response = restTemplate.getForEntity(url, ErgastResponse.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error calling Ergast API: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch driver standings from Ergast API", e);
        }
    }
    
    public ErgastResponse getRaceResults(int year) {
        String url = baseUrl + "/f1/" + year + "/results.json";
        log.debug("Calling Ergast API: {}", url);
        try {
            ResponseEntity<ErgastResponse> response = restTemplate.getForEntity(url, ErgastResponse.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error calling Ergast API: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch race results from Ergast API", e);
        }
    }
    
    public ErgastResponse getDriverInfo(String driverId) {
        String url = baseUrl + "/f1/drivers/" + driverId + ".json";
        log.debug("Calling Ergast API: {}", url);
        try {
            ResponseEntity<ErgastResponse> response = restTemplate.getForEntity(url, ErgastResponse.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error calling Ergast API: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch driver info from Ergast API", e);
        }
    }
    
    public ErgastResponse getRaceWinners(int year) {
        String url = baseUrl + "/f1/" + year + "/results/1.json";
        log.debug("Calling Ergast API: {}", url);
        try {
            ResponseEntity<ErgastResponse> response = restTemplate.getForEntity(url, ErgastResponse.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error calling Ergast API: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch race winners from Ergast API", e);
        }
    }
} 