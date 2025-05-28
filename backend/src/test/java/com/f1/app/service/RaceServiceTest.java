package com.f1.app.service;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import com.f1.app.dto.RaceDTO;
import com.f1.app.model.Race;
import com.f1.app.repository.RaceRepository;

class RaceServiceTest {
    private final int TEST_YEAR = 2023;
    
    @Mock
    private RaceRepository raceRepository;
    
    @Mock
    private ErgastApiService ergastApiService;
    
    @Mock
    private CacheService cacheService;
    
    @Mock
    private RedisCacheManager redisCacheManager;
    
    @Mock
    private org.springframework.cache.Cache redisCache;
    
    @InjectMocks
    private RaceService raceService;
    
    private List<Race> testRaces;
    private List<RaceDTO> testRaceDTOs;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(raceService, "baseUrl", "http://test-url");
        
        testRaces = new ArrayList<>();
        Race race = new Race();
        race.setSeason(TEST_YEAR);
        race.setRound(1);
        race.setRaceName("Test Race");
        testRaces.add(race);
        
        testRaceDTOs = new ArrayList<>();
        RaceDTO raceDTO = RaceDTO.fromEntity(race);
        testRaceDTOs.add(raceDTO);
        
        doNothing().when(cacheService).evictRaceCache(anyInt());
        
        // Mock Redis cache behavior
        when(redisCacheManager.getCache(anyString())).thenReturn(redisCache);
        when(redisCache.get(anyInt())).thenReturn(null);
    }

    @Test
    void getRacesByYear_WhenInDatabase_ReturnsFromDatabase() {
        // Arrange
        when(raceRepository.findBySeason(TEST_YEAR)).thenReturn(testRaces);

        // Act
        List<RaceDTO> result = raceService.getRacesByYear(TEST_YEAR);

        // Assert
        assertEquals(testRaceDTOs.size(), result.size());
        verify(raceRepository).findBySeason(TEST_YEAR);
        verify(ergastApiService, never()).fetchAndSaveRaces(anyInt(), anyString());
    }

    @Test
    void getRacesByYear_WhenNotInDatabase_FetchesFromAPI() {
        // Arrange
        when(raceRepository.findBySeason(TEST_YEAR)).thenReturn(new ArrayList<>());
        when(ergastApiService.fetchAndSaveRaces(eq(TEST_YEAR), anyString())).thenReturn(testRaceDTOs);

        // Act
        List<RaceDTO> result = raceService.getRacesByYear(TEST_YEAR);

        // Assert
        assertEquals(testRaceDTOs.size(), result.size());
        verify(raceRepository).findBySeason(TEST_YEAR);
        verify(ergastApiService).fetchAndSaveRaces(eq(TEST_YEAR), anyString());
    }

    @Test
    void getRacesByYear_WhenDatabaseThrowsException_FetchesFromAPI() {
        // Arrange
        when(raceRepository.findBySeason(TEST_YEAR))
            .thenThrow(new RuntimeException("Database error"));
        when(ergastApiService.fetchAndSaveRaces(eq(TEST_YEAR), anyString()))
            .thenReturn(testRaceDTOs);

        // Act
        List<RaceDTO> result = raceService.getRacesByYear(TEST_YEAR);

        // Assert
        assertEquals(testRaceDTOs.size(), result.size());
        verify(raceRepository).findBySeason(TEST_YEAR);
        verify(ergastApiService).fetchAndSaveRaces(eq(TEST_YEAR), anyString());
    }

    @Test
    void getRacesByYear_WhenAllFail_ReturnsEmptyList() {
        // Arrange
        when(raceRepository.findBySeason(TEST_YEAR))
            .thenThrow(new RuntimeException("Database error"));
        when(ergastApiService.fetchAndSaveRaces(eq(TEST_YEAR), anyString()))
            .thenReturn(new ArrayList<>());

        // Act
        List<RaceDTO> result = raceService.getRacesByYear(TEST_YEAR);

        // Assert
        assertTrue(result.isEmpty());
        verify(raceRepository).findBySeason(TEST_YEAR);
        verify(ergastApiService).fetchAndSaveRaces(eq(TEST_YEAR), anyString());
    }

    @Test
    void evictRaceCache_ShouldEvictCache() {
        // Arrange
        int year = 2023;
        
        // Act
        raceService.evictRaceCache(year);
        
        // Assert
        verify(cacheService).evictRaceCache(year);
    }
} 
