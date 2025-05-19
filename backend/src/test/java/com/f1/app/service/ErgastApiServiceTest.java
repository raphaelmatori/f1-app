package com.f1.app.service;

import com.f1.app.dto.ErgastChampionResponse;
import com.f1.app.model.Champion;
import com.f1.app.repository.ChampionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ErgastApiServiceTest {
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ChampionRepository championRepository;
    @InjectMocks
    private ErgastApiService ergastApiService;
    private final int TEST_YEAR = 2023;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void fetchWorldChampion_WhenApiReturnsChampion_SavesAndReturnsChampion() {
        ErgastChampionResponse response = mock(ErgastChampionResponse.class, RETURNS_DEEP_STUBS);
        Champion champion = new Champion(TEST_YEAR, "id", "code", "Max", "Verstappen", "Dutch", 454.0f, 19);
        when(restTemplate.getForObject(anyString(), eq(ErgastChampionResponse.class))).thenReturn(response);
        // Mock deep structure to simulate valid response
        var standingsTable = mock(ErgastChampionResponse.StandingsTable.class, RETURNS_DEEP_STUBS);
        var standingsList = mock(ErgastChampionResponse.StandingsList.class, RETURNS_DEEP_STUBS);
        var driverStanding = mock(ErgastChampionResponse.DriverStanding.class, RETURNS_DEEP_STUBS);
        var driver = mock(ErgastChampionResponse.Driver.class, RETURNS_DEEP_STUBS);
        when(response.getMrData()).thenReturn(mock(ErgastChampionResponse.MRData.class, RETURNS_DEEP_STUBS));
        when(response.getMrData().getStandingsTable()).thenReturn(standingsTable);
        when(standingsTable.getStandingsLists()).thenReturn(new ErgastChampionResponse.StandingsList[]{standingsList});
        when(standingsList.getDriverStandings()).thenReturn(new ErgastChampionResponse.DriverStanding[]{driverStanding});
        when(driverStanding.getDriver()).thenReturn(driver);
        when(driver.getDriverId()).thenReturn("id");
        when(driver.getCode()).thenReturn("code");
        when(driver.getGivenName()).thenReturn("Max");
        when(driver.getFamilyName()).thenReturn("Verstappen");
        when(driver.getNationality()).thenReturn("Dutch");
        when(driverStanding.getPoints()).thenReturn("454.0");
        when(driverStanding.getWins()).thenReturn("19");
        when(championRepository.save(any(Champion.class))).thenReturn(champion);
        ResponseEntity<Champion> result = ergastApiService.fetchWorldChampion(TEST_YEAR);
        assertEquals(ResponseEntity.ok(champion), result);
    }

    @Test
    void fetchWorldChampion_WhenApiReturnsNoData_ReturnsNotFound() {
        ErgastChampionResponse response = mock(ErgastChampionResponse.class, RETURNS_DEEP_STUBS);
        when(restTemplate.getForObject(anyString(), eq(ErgastChampionResponse.class))).thenReturn(response);
        when(response.getMrData()).thenReturn(null);
        ResponseEntity<Champion> result = ergastApiService.fetchWorldChampion(TEST_YEAR);
        assertEquals(ResponseEntity.notFound().build(), result);
    }

    @Test
    void fetchWorldChampion_WhenApiThrowsException_TriggersRetry() {
        when(restTemplate.getForObject(anyString(), eq(ErgastChampionResponse.class))).thenThrow(new RestClientException("API error"));
        assertThrows(RestClientException.class, () -> ergastApiService.fetchWorldChampion(TEST_YEAR));
    }

    @Test
    void fetchWorldChampion_WhenDbSaveFails_ReturnsChampion() {
        ErgastChampionResponse response = mock(ErgastChampionResponse.class, RETURNS_DEEP_STUBS);
        var standingsTable = mock(ErgastChampionResponse.StandingsTable.class, RETURNS_DEEP_STUBS);
        var standingsList = mock(ErgastChampionResponse.StandingsList.class, RETURNS_DEEP_STUBS);
        var driverStanding = mock(ErgastChampionResponse.DriverStanding.class, RETURNS_DEEP_STUBS);
        var driver = mock(ErgastChampionResponse.Driver.class, RETURNS_DEEP_STUBS);
        when(response.getMrData()).thenReturn(mock(ErgastChampionResponse.MRData.class, RETURNS_DEEP_STUBS));
        when(response.getMrData().getStandingsTable()).thenReturn(standingsTable);
        when(standingsTable.getStandingsLists()).thenReturn(new ErgastChampionResponse.StandingsList[]{standingsList});
        when(standingsList.getDriverStandings()).thenReturn(new ErgastChampionResponse.DriverStanding[]{driverStanding});
        when(driverStanding.getDriver()).thenReturn(driver);
        when(driver.getDriverId()).thenReturn("id");
        when(driver.getCode()).thenReturn("code");
        when(driver.getGivenName()).thenReturn("Max");
        when(driver.getFamilyName()).thenReturn("Verstappen");
        when(driver.getNationality()).thenReturn("Dutch");
        when(driverStanding.getPoints()).thenReturn("454.0");
        when(driverStanding.getWins()).thenReturn("19");
        when(restTemplate.getForObject(anyString(), eq(ErgastChampionResponse.class))).thenReturn(response);
        when(championRepository.save(any(Champion.class))).thenThrow(new RuntimeException("DB error"));
        ResponseEntity<Champion> result = ergastApiService.fetchWorldChampion(TEST_YEAR);
        assertEquals("Max", result.getBody().getGivenName());
    }
} 