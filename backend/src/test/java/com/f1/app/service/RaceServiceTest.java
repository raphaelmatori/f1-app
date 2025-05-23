package com.f1.app.service;

import com.f1.app.dto.RaceDTO;
import com.f1.app.dto.RaceResultDTO;
import com.f1.app.model.Race;
import com.f1.app.model.RaceResult;
import com.f1.app.repository.RaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RaceServiceTest {
    @Mock
    private RaceRepository raceRepository;
    @Mock
    private RedisCacheManager redisCacheManager;
    @Mock
    private ErgastApiService ergastApiService;
    @Mock
    private org.springframework.cache.Cache redisCache;
    @InjectMocks
    private RaceService raceService;
    private final int TEST_YEAR = 2023;
    private List<Race> testRaces;
    private List<RaceDTO> testRaceDTOs;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testRaces = new ArrayList<>();
        
        Race race = new Race();
        ReflectionTestUtils.setField(race, "season", TEST_YEAR);
        ReflectionTestUtils.setField(race, "round", 1);
        ReflectionTestUtils.setField(race, "raceName", "Test Grand Prix");
        ReflectionTestUtils.setField(race, "results", new ArrayList<>());
        
        RaceResult result = new RaceResult();
        ReflectionTestUtils.setField(result, "position", "1");
        ReflectionTestUtils.setField(result, "points", "25");
        ReflectionTestUtils.setField(result, "status", "Finished");
        ReflectionTestUtils.setField(result, "race", race);
        
        race.addResult(result);
        testRaces.add(race);

        testRaceDTOs = testRaces.stream()
            .map(RaceDTO::fromEntity)
            .collect(java.util.stream.Collectors.toList());

        when(redisCacheManager.getCache(anyString())).thenReturn(redisCache);
        when(ergastApiService.fetchAndSaveRaces(anyInt(), any())).thenReturn(testRaceDTOs);
    }

    @Test
    void getRacesByYear_WhenInRedisCache_ReturnsFromCache() {
        when(redisCache.get(TEST_YEAR)).thenReturn(() -> testRaceDTOs);
        List<RaceDTO> result = raceService.getRacesByYear(TEST_YEAR);
        assertNotNull(result);
        assertEquals(testRaces.size(), result.size());
        verify(raceRepository, never()).findBySeason(anyInt());
        verify(ergastApiService, never()).fetchAndSaveRaces(anyInt(), any());
    }

    @Test
    void getRacesByYear_WhenInDatabase_ReturnsFromDatabase() {
        when(redisCache.get(TEST_YEAR)).thenReturn(null);
        when(raceRepository.findBySeason(TEST_YEAR)).thenReturn(testRaces);
        List<RaceDTO> result = raceService.getRacesByYear(TEST_YEAR);
        assertNotNull(result);
        assertEquals(testRaces.size(), result.size());
        verify(ergastApiService, never()).fetchAndSaveRaces(anyInt(), any());
        verify(redisCache).put(eq(TEST_YEAR), any());
    }

    @Test
    void getRacesByYear_WhenNotCached_FetchesFromAPI() {
        when(redisCache.get(TEST_YEAR)).thenReturn(null);
        when(raceRepository.findBySeason(TEST_YEAR)).thenReturn(new ArrayList<>());
        when(ergastApiService.fetchAndSaveRaces(eq(TEST_YEAR), any())).thenReturn(testRaceDTOs);
        List<RaceDTO> result = raceService.getRacesByYear(TEST_YEAR);
        assertNotNull(result);
        assertEquals(testRaces.size(), result.size());
        verify(ergastApiService).fetchAndSaveRaces(eq(TEST_YEAR), any());
    }

    @Test
    void getRacesByYear_WhenRedisCacheThrowsException_ContinueWithDatabase() {
        when(redisCache.get(TEST_YEAR)).thenThrow(new RuntimeException("Cache error"));
        when(raceRepository.findBySeason(TEST_YEAR)).thenReturn(testRaces);
        List<RaceDTO> result = raceService.getRacesByYear(TEST_YEAR);
        assertNotNull(result);
        assertEquals(testRaces.size(), result.size());
        verify(ergastApiService, never()).fetchAndSaveRaces(anyInt(), any());
    }

    @Test
    void getRacesByYear_WhenDatabaseThrowsException_FetchesFromAPI() {
        when(redisCache.get(TEST_YEAR)).thenReturn(null);
        when(raceRepository.findBySeason(TEST_YEAR)).thenThrow(new RuntimeException("DB error"));
        when(ergastApiService.fetchAndSaveRaces(eq(TEST_YEAR), any())).thenReturn(testRaceDTOs);
        List<RaceDTO> result = raceService.getRacesByYear(TEST_YEAR);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getRacesByYear_WhenAllFail_ReturnsEmptyList() {
        when(redisCache.get(TEST_YEAR)).thenThrow(new RuntimeException("Cache error"));
        when(raceRepository.findBySeason(TEST_YEAR)).thenThrow(new RuntimeException("DB error"));
        when(ergastApiService.fetchAndSaveRaces(eq(TEST_YEAR), any())).thenReturn(new ArrayList<>());
        List<RaceDTO> result = raceService.getRacesByYear(TEST_YEAR);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getRacesByYear_WithLazyResults_SerializesCorrectly() {
        // Given
        RaceDTO race = RaceDTO.fromEntity(testRaces.get(0));
        List<RaceResultDTO> raceResults = (List<RaceResultDTO>) ReflectionTestUtils.getField(race, "results");
        assertFalse(raceResults.isEmpty(), "Test race should have results");
        
        // When
        when(redisCache.get(TEST_YEAR)).thenReturn(() -> testRaceDTOs);
        List<RaceDTO> result = raceService.getRacesByYear(TEST_YEAR);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        RaceDTO cachedRace = result.get(0);
        List<RaceResultDTO> cachedResults = (List<RaceResultDTO>) ReflectionTestUtils.getField(cachedRace, "results");
        assertNotNull(cachedResults);
        assertFalse(cachedResults.isEmpty());
        assertEquals(raceResults.size(), cachedResults.size());
        
        RaceResultDTO originalResult = raceResults.get(0);
        RaceResultDTO cachedResult = cachedResults.get(0);
        assertEquals(
            ReflectionTestUtils.getField(originalResult, "position"),
            ReflectionTestUtils.getField(cachedResult, "position")
        );
        assertEquals(
            ReflectionTestUtils.getField(originalResult, "points"),
            ReflectionTestUtils.getField(cachedResult, "points")
        );
    }
} 
