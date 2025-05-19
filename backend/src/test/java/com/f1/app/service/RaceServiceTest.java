package com.f1.app.service;

import com.f1.app.model.Race;
import com.f1.app.repository.RaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.cache.RedisCacheManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testRaces = new ArrayList<>();
        Race race = Race.builder().season(TEST_YEAR).round(1).raceName("Test Grand Prix").build();
        testRaces.add(race);
        when(redisCacheManager.getCache(anyString())).thenReturn(redisCache);
        when(ergastApiService.fetchAndSaveRaces(anyInt(), any())).thenReturn(testRaces);
    }

    @Test
    void getRacesByYear_WhenInRedisCache_ReturnsFromCache() {
        when(redisCache.get(TEST_YEAR)).thenReturn(() -> testRaces);
        List<Race> result = raceService.getRacesByYear(TEST_YEAR);
        assertNotNull(result);
        assertEquals(testRaces.size(), result.size());
        verify(raceRepository, never()).findBySeason(anyInt());
        verify(ergastApiService, never()).fetchAndSaveRaces(anyInt(), any());
    }

    @Test
    void getRacesByYear_WhenInDatabase_ReturnsFromDatabase() {
        when(redisCache.get(TEST_YEAR)).thenReturn(null);
        when(raceRepository.findBySeason(TEST_YEAR)).thenReturn(testRaces);
        List<Race> result = raceService.getRacesByYear(TEST_YEAR);
        assertNotNull(result);
        assertEquals(testRaces.size(), result.size());
        verify(ergastApiService, never()).fetchAndSaveRaces(anyInt(), any());
        verify(redisCache).put(eq(TEST_YEAR), any());
    }

    @Test
    void getRacesByYear_WhenNotCached_FetchesFromAPI() {
        when(redisCache.get(TEST_YEAR)).thenReturn(null);
        when(raceRepository.findBySeason(TEST_YEAR)).thenReturn(new ArrayList<>());
        when(ergastApiService.fetchAndSaveRaces(eq(TEST_YEAR), any())).thenReturn(testRaces);
        List<Race> result = raceService.getRacesByYear(TEST_YEAR);
        assertNotNull(result);
        assertEquals(testRaces.size(), result.size());
        verify(ergastApiService).fetchAndSaveRaces(eq(TEST_YEAR), any());
    }

    @Test
    void getRacesByYear_WhenRedisCacheThrowsException_ContinueWithDatabase() {
        when(redisCache.get(TEST_YEAR)).thenThrow(new RuntimeException("Cache error"));
        when(raceRepository.findBySeason(TEST_YEAR)).thenReturn(testRaces);
        List<Race> result = raceService.getRacesByYear(TEST_YEAR);
        assertNotNull(result);
        assertEquals(testRaces.size(), result.size());
        verify(ergastApiService, never()).fetchAndSaveRaces(anyInt(), any());
    }

    @Test
    void getRacesByYear_WhenDatabaseThrowsException_FetchesFromAPI() {
        when(redisCache.get(TEST_YEAR)).thenReturn(null);
        when(raceRepository.findBySeason(TEST_YEAR)).thenThrow(new RuntimeException("DB error"));
        when(ergastApiService.fetchAndSaveRaces(eq(TEST_YEAR), any())).thenReturn(testRaces);
        List<Race> result = raceService.getRacesByYear(TEST_YEAR);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getRacesByYear_WhenAllFail_ReturnsEmptyList() {
        when(redisCache.get(TEST_YEAR)).thenThrow(new RuntimeException("Cache error"));
        when(raceRepository.findBySeason(TEST_YEAR)).thenThrow(new RuntimeException("DB error"));
        when(ergastApiService.fetchAndSaveRaces(eq(TEST_YEAR), any())).thenReturn(new ArrayList<>());
        List<Race> result = raceService.getRacesByYear(TEST_YEAR);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
} 
