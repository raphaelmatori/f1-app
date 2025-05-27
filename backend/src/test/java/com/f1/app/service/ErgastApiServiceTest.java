package com.f1.app.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.Cache;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.f1.app.dto.ErgastChampionResponse;
import com.f1.app.dto.ErgastRaceResponse;
import com.f1.app.dto.RaceDTO;
import com.f1.app.model.Champion;
import com.f1.app.model.Race;
import com.f1.app.model.RaceResult;
import com.f1.app.repository.ChampionRepository;
import com.f1.app.repository.RaceRepository;

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
    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private ErgastApiService ergastApiService;

    private final int TEST_YEAR = 2023;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisCacheManager.getCache(anyString())).thenReturn(redisCache);
        when(applicationContext.getBean(ErgastApiService.class)).thenReturn(ergastApiService);
    }

    // --- Champion tests (existing) ---

    @Test
    void fetchWorldChampion_WhenApiReturnsChampion_SavesAndReturnsChampion() {
        // Create response structure using builder pattern
        ErgastChampionResponse response = ErgastChampionResponse.builder()
                .mrData(ErgastChampionResponse.MRData.builder()
                        .standingsTable(ErgastChampionResponse.StandingsTable.builder()
                                .standingsLists(new ErgastChampionResponse.StandingsList[]{
                                        ErgastChampionResponse.StandingsList.builder()
                                                .driverStandings(new ErgastChampionResponse.DriverStanding[]{
                                                        ErgastChampionResponse.DriverStanding.builder()
                                                                .driver(ErgastChampionResponse.Driver.builder()
                                                                        .driverId("id")
                                                                        .code("code")
                                                                        .givenName("Max")
                                                                        .familyName("Verstappen")
                                                                        .nationality("Dutch")
                                                                        .build())
                                                                .points("454.0")
                                                                .wins("19")
                                                                .build()
                                                })
                                                .build()
                                })
                                .build())
                        .build())
                .build();

        when(restTemplate.getForObject(anyString(), eq(ErgastChampionResponse.class))).thenReturn(response);

        Champion champion = Champion.builder()
                .year(TEST_YEAR)
                .driverId("id")
                .code("code")
                .givenName("Max")
                .familyName("Verstappen")
                .nationality("Dutch")
                .points(454.0f)
                .wins(19)
                .build();

        when(championRepository.save(any(Champion.class))).thenReturn(champion);

        ResponseEntity<Champion> result = ergastApiService.fetchWorldChampion(TEST_YEAR);

        assertNotNull(result);
        assertTrue(result.getStatusCode().is2xxSuccessful());
        assertNotNull(result.getBody());
        assertEquals("Max", result.getBody().getGivenName());
        assertEquals("Verstappen", result.getBody().getFamilyName());
        verify(championRepository).save(any(Champion.class));
    }

    @Test
    void fetchWorldChampion_WhenApiReturnsNoData_ReturnsNotFound() {
        ErgastChampionResponse response = new ErgastChampionResponse();
        when(restTemplate.getForObject(anyString(), eq(ErgastChampionResponse.class))).thenReturn(response);
        ResponseEntity<Champion> result = ergastApiService.fetchWorldChampion(TEST_YEAR);
        assertEquals(ResponseEntity.notFound().build(), result);
    }

    @Test
    void fetchWorldChampion_WhenApiThrowsException_TriggersRetry() {
        when(restTemplate.getForObject(anyString(), eq(ErgastChampionResponse.class)))
            .thenThrow(new RestClientException("API error"));
        assertThrows(RestClientException.class, () -> ergastApiService.fetchWorldChampion(TEST_YEAR));
    }

    @Test
    void fetchWorldChampion_WhenDbSaveFails_ThrowsException() {
        // Create response structure
        ErgastChampionResponse response = new ErgastChampionResponse();
        ErgastChampionResponse.MRData mrData = new ErgastChampionResponse.MRData();
        ErgastChampionResponse.StandingsTable standingsTable = new ErgastChampionResponse.StandingsTable();
        ErgastChampionResponse.StandingsList standingsList = new ErgastChampionResponse.StandingsList();
        ErgastChampionResponse.DriverStanding driverStanding = new ErgastChampionResponse.DriverStanding();
        ErgastChampionResponse.Driver driver = new ErgastChampionResponse.Driver();

        // Set up data
        driver.setDriverId("id");
        driver.setCode("code");
        driver.setGivenName("Max");
        driver.setFamilyName("Verstappen");
        driver.setNationality("Dutch");
        driverStanding.setDriver(driver);
        driverStanding.setPoints("454.0");
        driverStanding.setWins("19");
        standingsList.setDriverStandings(new ErgastChampionResponse.DriverStanding[]{driverStanding});
        standingsTable.setStandingsLists(new ErgastChampionResponse.StandingsList[]{standingsList});
        mrData.setStandingsTable(standingsTable);
        response.setMrData(mrData);

        when(restTemplate.getForObject(anyString(), eq(ErgastChampionResponse.class))).thenReturn(response);
        when(championRepository.save(any(Champion.class))).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> ergastApiService.fetchWorldChampion(TEST_YEAR));
    }

    @Test
    void fetchAndSaveRaces_WhenDatabaseSaveFails_ThrowsException() {
        // Mock response
        ErgastRaceResponse response = new ErgastRaceResponse();
        ErgastRaceResponse.MRData mrData = new ErgastRaceResponse.MRData();
        ErgastRaceResponse.RaceTable raceTable = new ErgastRaceResponse.RaceTable();
        ErgastRaceResponse.RaceData raceData = createMockRaceData("1", "Test Race");

        mrData.setTotal("1");
        raceTable.setRaces(List.of(raceData));
        mrData.setRaceTable(raceTable);
        response.setMrData(mrData);

        when(restTemplate.getForEntity(anyString(), eq(ErgastRaceResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        // Mock repository to throw exception
        when(raceRepository.save(any(Race.class))).thenThrow(new RuntimeException("Database error"));

        // Execute and verify
        assertThrows(RuntimeException.class, () -> ergastApiService.fetchAndSaveRaces(2023, "http://fake-url"));
    }

    // Helper method to create mock race data
    private ErgastRaceResponse.RaceData createMockRaceData(String round, String raceName) {
        ErgastRaceResponse.RaceData raceData = new ErgastRaceResponse.RaceData();
        ErgastRaceResponse.CircuitData circuit = new ErgastRaceResponse.CircuitData();
        ErgastRaceResponse.LocationData location = new ErgastRaceResponse.LocationData();
        ErgastRaceResponse.ResultData results = new ErgastRaceResponse.ResultData();
        ErgastRaceResponse.DriverData driver = new ErgastRaceResponse.DriverData();
        ErgastRaceResponse.ConstructorData constructor = new ErgastRaceResponse.ConstructorData();
        ErgastRaceResponse.TimeData time = new ErgastRaceResponse.TimeData();

        raceData.setSeason("2023");
        raceData.setRound(round);
        raceData.setRaceName(raceName);
        raceData.setDate("2023-03-05");
        raceData.setTime("15:00:00Z");
        raceData.setCircuit(circuit);
        raceData.setResults(List.of(results));

        circuit.setCircuitId("test_circuit");
        circuit.setCircuitName("Test Circuit");
        circuit.setLocation(location);
        location.setLocality("Test City");
        location.setCountry("Test Country");

        results.setPosition("1");
        results.setPoints("25");
        results.setGrid("1");
        results.setLaps("58");
        results.setStatus("Finished");
        results.setDriver(driver);
        results.setConstructor(constructor);
        results.setTime(time);

        driver.setDriverId("test_driver");
        driver.setCode("TST");
        driver.setGivenName("Test");
        driver.setFamilyName("Driver");
        driver.setNationality("Test Nationality");

        constructor.setConstructorId("test_constructor");
        constructor.setName("Test Team");
        constructor.setNationality("Test Constructor Nationality");

        time.setMillis("5000");
        time.setTime("1:23.456");

        return raceData;
    }

    // --- New: mapToRace tests ---

    @Test
    void mapToRace_WithValidData_ReturnsRace() {
        ErgastRaceResponse.RaceData raceData = new ErgastRaceResponse.RaceData();
        raceData.setSeason("2023");
        raceData.setRound("1");
        raceData.setRaceName("Test GP");
        raceData.setDate("2023-03-01");
        raceData.setTime("12:00:00Z");
        raceData.setCircuit(null);
        raceData.setResults(null);

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
        ErgastRaceResponse.RaceData raceData = new ErgastRaceResponse.RaceData();
        raceData.setSeason(null);
        raceData.setRound(null);
        Race race = invokeMapToRace(raceData);
        assertNull(race);
    }

    @Test
    void mapToRace_WithFullResultData_MapsNestedObjects() {
        ErgastRaceResponse.RaceData raceData = ErgastRaceResponse.RaceData.builder()
                .season("2023")
                .round("1")
                .raceName("Test GP")
                .date("2023-03-01")
                .time("12:00:00Z")
                .results(List.of(ErgastRaceResponse.ResultData.builder()
                        .position("1")
                        .points("25")
                        .grid("1")
                        .laps("58")
                        .status("Finished")
                        .driver(ErgastRaceResponse.DriverData.builder()
                                .driverId("hamilton")
                                .code("HAM")
                                .givenName("Lewis")
                                .familyName("Hamilton")
                                .nationality("British")
                                .build())
                        .constructor(ErgastRaceResponse.ConstructorData.builder()
                                .constructorId("mercedes")
                                .name("Mercedes")
                                .nationality("German")
                                .build())
                        .time(ErgastRaceResponse.TimeData.builder()
                                .millis("5400000")
                                .time("1:30:00.000")
                                .build())
                        .build()))
                .build();

        Race race = invokeMapToRace(raceData);

        assertNotNull(race);
        assertNotNull(race.getResults());
        assertEquals(1, race.getResults().size());
        RaceResult rr = race.getResults().get(0);
        assertNotNull(rr);
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

    @Test
    void fetchAndSaveRaces_WhenApiReturnsEmptyBody_ThrowsException() {
        when(restTemplate.getForEntity(anyString(), eq(ErgastRaceResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            ergastApiService.fetchAndSaveRaces(TEST_YEAR, "http://test-url"));
        assertEquals("Failed to fetch races", exception.getMessage());
    }

    @Test
    void fetchAndSaveRaces_WhenInvalidSeasonFormat_HandlesError() {
        ErgastRaceResponse response = ErgastRaceResponse.builder()
                .mrData(ErgastRaceResponse.MRData.builder()
                        .total("0")
                        .raceTable(ErgastRaceResponse.RaceTable.builder()
                                .races(new ArrayList<>())
                                .build())
                        .build())
                .build();

        when(restTemplate.getForEntity(anyString(), eq(ErgastRaceResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        List<RaceDTO> result = ergastApiService.fetchAndSaveRaces(TEST_YEAR, "http://test-url");
        assertTrue(result.isEmpty());
    }

    @Test
    void saveRacesInRedisAsync_WhenCacheThrowsException_HandlesError() {
        List<Race> races = new ArrayList<>();
        Race race = Race.builder()
                .season(TEST_YEAR)
                .round(1)
                .raceName("Test Race")
                .build();
        races.add(race);

        List<RaceDTO> raceDTOs = races.stream()
                .map(RaceDTO::fromEntity)
                .collect(Collectors.toList());

        when(redisCacheManager.getCache(anyString())).thenReturn(redisCache);
        doThrow(new RuntimeException("Cache error"))
                .when(redisCache).put(any(), any());

        CompletableFuture<Void> future = ergastApiService.saveRacesInRedisAsync(TEST_YEAR, raceDTOs);
        assertDoesNotThrow(() -> future.get(1, TimeUnit.SECONDS));
    }

    @Test
    void saveRacesInDatabaseAsync_WhenSaveSucceeds_ReturnsCompletedFuture() {
        List<Race> races = new ArrayList<>();
        Race race = Race.builder()
                .season(TEST_YEAR)
                .round(1)
                .build();
        races.add(race);

        when(raceRepository.save(any(Race.class))).thenReturn(race);

        CompletableFuture<Void> future = ergastApiService.saveRacesInDatabaseAsync(races, TEST_YEAR);
        assertNotNull(future);
        assertDoesNotThrow(() -> future.get());
        verify(raceRepository).save(any(Race.class));
    }

    @Test
    void fetchAndSaveRaces_WhenMergingDuplicateRaces_MergesResults() {
        // First response with one race
        ErgastRaceResponse firstResponse = ErgastRaceResponse.builder()
                .mrData(ErgastRaceResponse.MRData.builder()
                        .total("101")
                        .raceTable(ErgastRaceResponse.RaceTable.builder()
                                .races(List.of(createRaceData("1", "HAM")))
                                .build())
                        .build())
                .build();

        // Second response with same race but different result
        ErgastRaceResponse secondResponse = ErgastRaceResponse.builder()
                .mrData(ErgastRaceResponse.MRData.builder()
                        .total("101")
                        .raceTable(ErgastRaceResponse.RaceTable.builder()
                                .races(List.of(createRaceData("2", "VER")))
                                .build())
                        .build())
                .build();

        when(restTemplate.getForEntity(contains("offset=0"), eq(ErgastRaceResponse.class)))
                .thenReturn(ResponseEntity.ok(firstResponse));
        when(restTemplate.getForEntity(contains("offset=100"), eq(ErgastRaceResponse.class)))
                .thenReturn(ResponseEntity.ok(secondResponse));

        // Mock repository save to return the same race
        when(raceRepository.save(any(Race.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<RaceDTO> result = ergastApiService.fetchAndSaveRaces(TEST_YEAR, "http://test-url");
        
        assertNotNull(result);
        assertEquals(1, result.size());
        RaceDTO raceDTO = result.get(0);
        assertNotNull(raceDTO.getResults());
        assertEquals(2, raceDTO.getResults().size());
        
        verify(restTemplate, times(2)).getForEntity(anyString(), eq(ErgastRaceResponse.class));
    }

    private ErgastRaceResponse.RaceData createRaceData(String position, String driverCode) {
        return ErgastRaceResponse.RaceData.builder()
                .season("2023")
                .round("1")
                .raceName("Test Race")
                .date("2023-03-05")
                .time("15:00:00Z")
                .circuit(ErgastRaceResponse.CircuitData.builder()
                        .circuitId("test_circuit")
                        .circuitName("Test Circuit")
                        .location(ErgastRaceResponse.LocationData.builder()
                                .locality("Test City")
                                .country("Test Country")
                                .build())
                        .build())
                .results(List.of(ErgastRaceResponse.ResultData.builder()
                        .position(position)
                        .points(position.equals("1") ? "25" : "18")
                        .grid(position)
                        .laps("58")
                        .status("Finished")
                        .driver(ErgastRaceResponse.DriverData.builder()
                                .driverId(driverCode.toLowerCase())
                                .code(driverCode)
                                .givenName(driverCode.equals("HAM") ? "Lewis" : "Max")
                                .familyName(driverCode.equals("HAM") ? "Hamilton" : "Verstappen")
                                .nationality(driverCode.equals("HAM") ? "British" : "Dutch")
                                .build())
                        .constructor(ErgastRaceResponse.ConstructorData.builder()
                                .constructorId(driverCode.toLowerCase() + "_team")
                                .name(driverCode.equals("HAM") ? "Mercedes" : "Red Bull")
                                .nationality(driverCode.equals("HAM") ? "German" : "Austrian")
                                .build())
                        .time(ErgastRaceResponse.TimeData.builder()
                                .millis("5400000")
                                .time("1:30:00.000")
                                .build())
                        .build()))
                .build();
    }

    @Test
    void mapToRace_WhenNullCircuitLocation_HandlesGracefully() {
        ErgastRaceResponse.RaceData raceData = ErgastRaceResponse.RaceData.builder()
                .season("2023")
                .round("1")
                .raceName("Test GP")
                .date("2023-03-01")
                .time("12:00:00Z")
                .circuit(ErgastRaceResponse.CircuitData.builder()
                        .circuitId("test_circuit")
                        .circuitName("Test Circuit")
                        .location(null)
                        .build())
                .build();
        
        Race race = invokeMapToRace(raceData);
        
        assertNotNull(race);
        assertNotNull(race.getCircuit());
        assertNull(race.getCircuit().getLocality());
        assertNull(race.getCircuit().getCountry());
    }

    @Test
    void mapToRace_WhenNullResultData_SkipsResult() {
        ErgastRaceResponse.RaceData raceData = ErgastRaceResponse.RaceData.builder()
                .season("2023")
                .round("1")
                .raceName("Test GP")
                .results(new ArrayList<>())
                .build();
        
        Race race = invokeMapToRace(raceData);
        
        assertNotNull(race);
        assertNotNull(race.getResults());
        assertEquals(0, race.getResults().size());
    }

    // Additional test cases for better coverage
    @Test
    void fetchAndSaveRaces_WhenApiReturnsNullMRData_ThrowsException() {
        ErgastRaceResponse response = ErgastRaceResponse.builder()
                .mrData(null)
                .build();

        when(restTemplate.getForEntity(anyString(), eq(ErgastRaceResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            ergastApiService.fetchAndSaveRaces(TEST_YEAR, "http://test-url"));
        assertEquals("Failed to fetch races", exception.getMessage());
    }

    @Test
    void fetchAndSaveRaces_WhenApiReturnsNullRaceTable_ThrowsException() {
        ErgastRaceResponse response = ErgastRaceResponse.builder()
                .mrData(ErgastRaceResponse.MRData.builder()
                        .total("1")
                        .raceTable(null)
                        .build())
                .build();

        when(restTemplate.getForEntity(anyString(), eq(ErgastRaceResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            ergastApiService.fetchAndSaveRaces(TEST_YEAR, "http://test-url"));
        assertEquals("Failed to fetch races", exception.getMessage());
    }

    @Test
    void fetchAndSaveRaces_WhenApiReturnsNullRaces_ReturnsEmptyList() {
        ErgastRaceResponse response = ErgastRaceResponse.builder()
                .mrData(ErgastRaceResponse.MRData.builder()
                        .total("0")
                        .raceTable(ErgastRaceResponse.RaceTable.builder()
                                .races(null)
                                .build())
                        .build())
                .build();

        when(restTemplate.getForEntity(anyString(), eq(ErgastRaceResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        List<RaceDTO> result = ergastApiService.fetchAndSaveRaces(TEST_YEAR, "http://test-url");
        assertTrue(result.isEmpty());
    }

    @Test
    void mapToRace_WhenConstructorAndTimeDataPresent_MapsAllFields() {
        ErgastRaceResponse.RaceData raceData = ErgastRaceResponse.RaceData.builder()
                .season("2023")
                .round("1")
                .raceName("Test GP")
                .results(List.of(ErgastRaceResponse.ResultData.builder()
                        .position("1")
                        .points("25")
                        .grid("1")
                        .laps("58")
                        .status("Finished")
                        .constructor(ErgastRaceResponse.ConstructorData.builder()
                                .constructorId("mercedes")
                                .name("Mercedes")
                                .nationality("German")
                                .build())
                        .time(ErgastRaceResponse.TimeData.builder()
                                .millis("5400000")
                                .time("1:30:00.000")
                                .build())
                        .build()))
                .build();

        Race race = invokeMapToRace(raceData);
        assertNotNull(race);
        assertEquals(1, race.getResults().size());
        RaceResult result = race.getResults().get(0);
        assertNotNull(result.getConstructor());
        assertEquals("mercedes", result.getConstructor().getConstructorId());
        assertNotNull(result.getTime());
        assertEquals("5400000", result.getTime().getMillis());
    }
}
