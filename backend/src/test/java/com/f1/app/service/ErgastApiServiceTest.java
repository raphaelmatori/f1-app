package com.f1.app.service;

import com.f1.app.dto.ErgastChampionResponse;
import com.f1.app.dto.ErgastRaceResponse;
import com.f1.app.model.Champion;
import com.f1.app.model.Race;
import com.f1.app.model.RaceResult;
import com.f1.app.repository.ChampionRepository;
import com.f1.app.repository.RaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.cache.Cache;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ErgastApiServiceTest {
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ChampionRepository championRepository;
    @Mock
    private RaceRepository raceRepository;
    @Mock
    private RedisCacheManager redisCacheManager;
    @Mock
    private Cache redisCache;

    @InjectMocks
    private ErgastApiService ergastApiService;

    private final int TEST_YEAR = 2023;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisCacheManager.getCache(anyString())).thenReturn(redisCache);
    }

    // --- Champion tests (existing) ---

    @Test
    void fetchWorldChampion_WhenApiReturnsChampion_SavesAndReturnsChampion() {
        ErgastChampionResponse response = mock(ErgastChampionResponse.class, RETURNS_DEEP_STUBS);
        Champion champion = new Champion(TEST_YEAR, "id", "code", "Max", "Verstappen", "Dutch", 454.0f, 19);
        when(restTemplate.getForObject(anyString(), eq(ErgastChampionResponse.class))).thenReturn(response);
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

    // --- New: fetchAndSaveRaces tests ---

    @Test
    void fetchAndSaveRaces_WhenApiReturnsRaces_SavesAndCaches() {
        ErgastRaceResponse.MRData mrData = mock(ErgastRaceResponse.MRData.class, RETURNS_DEEP_STUBS);
        ErgastRaceResponse.RaceTable raceTable = mock(ErgastRaceResponse.RaceTable.class, RETURNS_DEEP_STUBS);
        ErgastRaceResponse.RaceData raceData = mock(ErgastRaceResponse.RaceData.class, RETURNS_DEEP_STUBS);

        when(raceData.getSeason()).thenReturn("2023");
        when(raceData.getRound()).thenReturn("1");
        when(raceTable.getRaces()).thenReturn(List.of(new ErgastRaceResponse.RaceData[]{raceData}));
        when(mrData.getTotal()).thenReturn("1");
        when(mrData.getRaceTable()).thenReturn(raceTable);

        ErgastRaceResponse response = mock(ErgastRaceResponse.class, RETURNS_DEEP_STUBS);
        when(response.getMrData()).thenReturn(mrData);

        when(restTemplate.getForEntity(anyString(), eq(ErgastRaceResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        List<Race> savedRaces = new ArrayList<>();
        doAnswer(invocation -> {
            Race race = invocation.getArgument(0);
            savedRaces.add(race);
            return race;
        }).when(raceRepository).save(any(Race.class));

        List<Race> result = ergastApiService.fetchAndSaveRaces(2023, "http://fake-url");
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(raceRepository, atLeastOnce()).save(any(Race.class));
        verify(redisCache, atLeastOnce()).put(eq(2023), any());
    }

    @Test
    void fetchAndSaveRaces_WhenApiReturnsNoRaces_ReturnsEmptyList() {
        ErgastRaceResponse.MRData mrData = mock(ErgastRaceResponse.MRData.class, RETURNS_DEEP_STUBS);
        ErgastRaceResponse.RaceTable raceTable = mock(ErgastRaceResponse.RaceTable.class, RETURNS_DEEP_STUBS);

        when(raceTable.getRaces()).thenReturn(List.of(new ErgastRaceResponse.RaceData[0]));
        when(mrData.getTotal()).thenReturn("0");
        when(mrData.getRaceTable()).thenReturn(raceTable);

        ErgastRaceResponse response = mock(ErgastRaceResponse.class, RETURNS_DEEP_STUBS);
        when(response.getMrData()).thenReturn(mrData);

        when(restTemplate.getForEntity(anyString(), eq(ErgastRaceResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        List<Race> result = ergastApiService.fetchAndSaveRaces(2023, "http://fake-url");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchAndSaveRaces_WhenApiThrowsException_Throws() {
        when(restTemplate.getForEntity(anyString(), eq(ErgastRaceResponse.class)))
                .thenThrow(new RestClientException("API error"));
        assertThrows(RestClientException.class, () -> ergastApiService.fetchAndSaveRaces(2023, "http://fake-url"));
    }

    // --- New: mapToRace tests ---

    @Test
    void mapToRace_WithValidData_ReturnsRace() {
        ErgastRaceResponse.RaceData raceData = mock(ErgastRaceResponse.RaceData.class, RETURNS_DEEP_STUBS);
        when(raceData.getSeason()).thenReturn("2023");
        when(raceData.getRound()).thenReturn("1");
        when(raceData.getRaceName()).thenReturn("Test GP");
        when(raceData.getDate()).thenReturn("2023-03-01");
        when(raceData.getTime()).thenReturn("12:00:00Z");
        when(raceData.getCircuit()).thenReturn(null);
        when(raceData.getResults()).thenReturn(null);

        Race race = invokeMapToRace(raceData);
        assertNotNull(race);
        assertEquals(2023, race.getSeason());
        assertEquals(1, race.getRound());
        assertEquals("Test GP", race.getRaceName());
    }

    @Test
    void mapToRace_WithNullData_ReturnsNull() {
        Race race = invokeMapToRace(null);
        assertNull(race);
    }

    @Test
    void mapToRace_WithMissingSeasonOrRound_ReturnsNull() {
        ErgastRaceResponse.RaceData raceData = mock(ErgastRaceResponse.RaceData.class, RETURNS_DEEP_STUBS);
        when(raceData.getSeason()).thenReturn(null);
        when(raceData.getRound()).thenReturn(null);
        Race race = invokeMapToRace(raceData);
        assertNull(race);
    }

    @Test
    void mapToRace_WithFullResultData_MapsNestedObjects() {
        // Mock nested data
        ErgastRaceResponse.RaceData raceData = mock(ErgastRaceResponse.RaceData.class, RETURNS_DEEP_STUBS);
        when(raceData.getSeason()).thenReturn("2023");
        when(raceData.getRound()).thenReturn("1");
        when(raceData.getRaceName()).thenReturn("Test GP");
        when(raceData.getDate()).thenReturn("2023-03-01");
        when(raceData.getTime()).thenReturn("12:00:00Z");
        when(raceData.getCircuit()).thenReturn(null);

        // ResultData
        ErgastRaceResponse.ResultData resultData = mock(ErgastRaceResponse.ResultData.class, RETURNS_DEEP_STUBS);
        when(resultData.getPosition()).thenReturn("1");
        when(resultData.getPoints()).thenReturn("25");
        when(resultData.getGrid()).thenReturn("1");
        when(resultData.getLaps()).thenReturn("58");
        when(resultData.getStatus()).thenReturn("Finished");

        // DriverData
        ErgastRaceResponse.DriverData driverData = mock(ErgastRaceResponse.DriverData.class, RETURNS_DEEP_STUBS);
        when(driverData.getDriverId()).thenReturn("hamilton");
        when(driverData.getCode()).thenReturn("HAM");
        when(driverData.getGivenName()).thenReturn("Lewis");
        when(driverData.getFamilyName()).thenReturn("Hamilton");
        when(driverData.getNationality()).thenReturn("British");
        when(resultData.getDriver()).thenReturn(driverData);

        // ConstructorData
        ErgastRaceResponse.ConstructorData constructorData = mock(ErgastRaceResponse.ConstructorData.class, RETURNS_DEEP_STUBS);
        when(constructorData.getConstructorId()).thenReturn("mercedes");
        when(constructorData.getName()).thenReturn("Mercedes");
        when(constructorData.getNationality()).thenReturn("German");
        when(resultData.getConstructor()).thenReturn(constructorData);

        // TimeData
        ErgastRaceResponse.TimeData timeData = mock(ErgastRaceResponse.TimeData.class, RETURNS_DEEP_STUBS);
        when(timeData.getMillis()).thenReturn("5400000");
        when(timeData.getTime()).thenReturn("1:30:00.000");
        when(resultData.getTime()).thenReturn(timeData);

        // Set results
        when(raceData.getResults()).thenReturn(List.of(new ErgastRaceResponse.ResultData[]{resultData}));

        Race race = invokeMapToRace(raceData);

        assertNotNull(race);
        assertEquals(1, race.getResults().size());
        RaceResult rr = race.getResults().get(0);
        assertEquals("1", rr.getPosition());
        assertEquals("25", rr.getPoints());
        assertEquals("1", rr.getGrid());
        assertEquals("58", rr.getLaps());
        assertEquals("Finished", rr.getStatus());
        assertNotNull(rr.getDriver());
        assertEquals("hamilton", rr.getDriver().getDriverId());
        assertEquals("HAM", rr.getDriver().getCode());
        assertEquals("Lewis", rr.getDriver().getGivenName());
        assertEquals("Hamilton", rr.getDriver().getFamilyName());
        assertEquals("British", rr.getDriver().getNationality());
        assertNotNull(rr.getConstructor());
        assertEquals("mercedes", rr.getConstructor().getConstructorId());
        assertEquals("Mercedes", rr.getConstructor().getName());
        assertEquals("German", rr.getConstructor().getNationality());
        assertNotNull(rr.getTime());
        assertEquals("5400000", rr.getTime().getMillis());
        assertEquals("1:30:00.000", rr.getTime().getTime());
    }

    // Helper to access private method
    private Race invokeMapToRace(ErgastRaceResponse.RaceData raceData) {
        try {
            var method = ErgastApiService.class.getDeclaredMethod("mapToRace", ErgastRaceResponse.RaceData.class);
            method.setAccessible(true);
            return (Race) method.invoke(ergastApiService, raceData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
