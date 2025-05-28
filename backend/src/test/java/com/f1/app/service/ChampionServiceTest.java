package com.f1.app.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.f1.app.dto.ChampionDTO;
import com.f1.app.exception.ServiceException;
import com.f1.app.model.Champion;
import com.f1.app.model.SeasonInfo;
import com.f1.app.repository.ChampionRepository;
import com.f1.app.repository.SeasonInfoRepository;

class ChampionServiceTest {

    @Mock
    private ChampionRepository championRepository;

    @Mock
    private SeasonInfoRepository seasonInfoRepository;

    @Mock
    private ErgastApiService ergastApiService;

    @Mock
    private CacheService cacheService;

    @Mock
    private RedisCacheManager redisCacheManager;

    @Mock
    private Cache redisCache;

    @InjectMocks
    private ChampionService championService;

    private Champion testChampion;
    private ChampionDTO testChampionDTO;
    private final Integer currentYear = LocalDate.now().getYear();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(championService, "redisCacheManager", redisCacheManager);
        
        // Mock Redis cache behavior
        when(redisCacheManager.getCache(anyString())).thenReturn(redisCache);
        when(redisCache.get(anyInt())).thenReturn(null);
        
        testChampion = new Champion();
        testChampion.setYear(currentYear);
        testChampion.setDriverId("test_driver");
        testChampionDTO = ChampionDTO.fromEntity(testChampion);
        
        doNothing().when(cacheService).evictChampionCache(anyString());
    }

    @Test
    void getChampions_ShouldReturnAllChampions() {
        // Arrange
        List<Champion> expectedChampions = Arrays.asList(
                testChampion,
                new Champion(2022, "verstappen", "VER", "Max", "Verstappen", "Dutch", 454.0f, 15)
        );
        when(championRepository.findAll()).thenReturn(expectedChampions);
        
        // Mock SeasonInfo for current year
        SeasonInfo seasonInfo = SeasonInfo.builder()
            .year(currentYear)
            .isChampionAvailableForCurrentYear(true)
            .build();
        when(seasonInfoRepository.findByYear(currentYear)).thenReturn(seasonInfo);

        // Act
        ResponseEntity<List<ChampionDTO>> response = championService.getChampions();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ChampionDTO> champions = response.getBody();
        assertEquals(2, champions.size());
        assertEquals(expectedChampions.get(0).getDriverId(), champions.get(0).getDriverId());
        verify(championRepository).findAll();
        verify(seasonInfoRepository).findByYear(currentYear);
    }

    @Test
    void getChampions_WhenNoChampions_ShouldThrowServiceException() {
        // Arrange
        when(championRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> championService.getChampions());
        assertEquals("No champions found", exception.getMessage());
        assertEquals("NO_CHAMPIONS_FOUND", exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getStatus());
    }

    @Test
    void getChampion_WhenExistsInDatabase_ShouldReturnChampion() {
        when(championRepository.findByYear(2023)).thenReturn(Optional.of(testChampion));

        ResponseEntity<ChampionDTO> response = championService.getChampion(2023);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ChampionDTO champion = response.getBody();
        assertEquals(testChampion.getDriverId(), champion.getDriverId());
        verify(championRepository).findByYear(2023);
        verify(ergastApiService, never()).fetchWorldChampion(anyInt());
    }

    @Test
    void getChampion_WhenNotInDatabase_ShouldFetchFromApi() {
        when(championRepository.findByYear(2023)).thenReturn(Optional.empty());
        when(ergastApiService.fetchWorldChampion(2023)).thenReturn(ResponseEntity.ok(testChampion));
        when(championRepository.save(testChampion)).thenReturn(testChampion);

        ResponseEntity<ChampionDTO> response = championService.getChampion(2023);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ChampionDTO champion = response.getBody();
        assertEquals(testChampion.getDriverId(), champion.getDriverId());
        verify(championRepository).findByYear(2023);
        verify(ergastApiService).fetchWorldChampion(2023);
        verify(championRepository).save(testChampion);
    }

    @Test
    void getChampion_WhenApiReturnsError_ShouldThrowServiceException() {
        // Arrange
        int year = 2023;
        when(championRepository.findByYear(year)).thenReturn(Optional.empty());
        when(ergastApiService.fetchWorldChampion(year)).thenReturn(ResponseEntity.notFound().build());

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> championService.getChampion(year));
        assertEquals(String.format("No champion found for year %d", year), exception.getMessage());
        assertEquals("CHAMPION_NOT_FOUND", exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getStatus());
    }

    @Test
    void getChampion_WhenSaveFails_ShouldStillReturnChampion() {
        when(championRepository.findByYear(2023)).thenReturn(Optional.empty());
        when(ergastApiService.fetchWorldChampion(2023)).thenReturn(ResponseEntity.ok(testChampion));
        when(championRepository.save(testChampion)).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<ChampionDTO> response = championService.getChampion(2023);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ChampionDTO champion = response.getBody();
        assertEquals(testChampion.getDriverId(), champion.getDriverId());
        verify(championRepository).findByYear(2023);
        verify(ergastApiService).fetchWorldChampion(2023);
        verify(championRepository).save(testChampion);
    }

    @Test
    void getChampion_WhenCurrentYearAndChampionNotAvailable_ShouldThrowServiceException() {
        // Arrange
        SeasonInfo seasonInfo = SeasonInfo.builder()
            .year(currentYear)
            .isChampionAvailableForCurrentYear(false)
            .build();
        
        when(seasonInfoRepository.findByYear(currentYear)).thenReturn(seasonInfo);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> championService.getChampion(currentYear));
        assertEquals(String.format("Champion data for year %d is not yet available", currentYear), exception.getMessage());
        assertEquals("CHAMPION_NOT_AVAILABLE", exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getStatus());
    }

    @Test
    void getChampion_WhenPastYear_ShouldReturnChampion() {
        // Arrange
        int pastYear = currentYear - 1;
        Champion pastChampion = new Champion(pastYear, "verstappen", "VER", "Max", "Verstappen", "Dutch", 454.0f, 15);
        when(championRepository.findByYear(pastYear)).thenReturn(Optional.of(pastChampion));

        // Act
        ResponseEntity<ChampionDTO> response = championService.getChampion(pastYear);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ChampionDTO champion = response.getBody();
        assertEquals(pastYear, champion.getYear());
    }

    @Test
    void getChampion_WhenCurrentYearAndChampionAvailable_ShouldReturnChampion() {
        // Arrange
        SeasonInfo seasonInfo = SeasonInfo.builder()
            .year(currentYear)
            .isChampionAvailableForCurrentYear(true)
            .build();
        
        when(seasonInfoRepository.findByYear(currentYear)).thenReturn(seasonInfo);
        when(championRepository.findByYear(currentYear)).thenReturn(Optional.of(testChampion));

        // Act
        ResponseEntity<ChampionDTO> response = championService.getChampion(currentYear);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ChampionDTO champion = response.getBody();
        assertEquals(currentYear, champion.getYear());
    }

    @Test
    void evictCurrentYearCache_ShouldEvictFromBothCaches() {
        // Act
        championService.evictCurrentYearCache();
        
        // Assert
        verify(cacheService).evictChampionCache("currentYear");
    }

    @Test
    void evictAllChampionsCache_ShouldEvictFromBothCaches() {
        // Act
        championService.evictAllChampionsCache();
        
        // Assert
        verify(cacheService).evictChampionCache("allChampions");
    }

    @Test
    void initializeChampionData_ShouldEvictCurrentYearCacheBeforeUpdate() {
        // Arrange
        when(championRepository.findAll()).thenReturn(Collections.emptyList());
        when(ergastApiService.fetchWorldChampion(anyInt())).thenReturn(ResponseEntity.ok(testChampion));
        
        // Act
        championService.initializeChampionData();
        
        // Assert
        verify(cacheService).evictChampionCache("currentYear");
        verify(championRepository).findAll();
        verify(ergastApiService).fetchWorldChampion(currentYear);
    }

    @Test
    void initializeChampionData_WhenCurrentYearFails_ShouldContinueWithPastYears() {
        // Arrange
        when(championRepository.findAll()).thenReturn(Collections.emptyList());
        when(ergastApiService.fetchWorldChampion(currentYear))
            .thenThrow(new RuntimeException("API error"));
        
        // Act
        championService.initializeChampionData();
        
        // Assert
        verify(cacheService).evictChampionCache("currentYear");
        verify(championRepository).findAll();
        verify(ergastApiService).fetchWorldChampion(currentYear);
    }

    @Test
    void initializeChampionData_ShouldAlwaysUpdateCurrentYear() {
        // Arrange
        when(championRepository.findAll()).thenReturn(Collections.emptyList());
        when(ergastApiService.fetchWorldChampion(anyInt())).thenReturn(ResponseEntity.ok(testChampion));
        
        // Act
        championService.initializeChampionData();
        
        // Assert
        verify(cacheService).evictChampionCache("currentYear");
        verify(championRepository).findAll();
        verify(ergastApiService).fetchWorldChampion(currentYear);
    }

    @Test
    void getChampions_WhenCurrentYearChampionNotAvailable_ShouldExcludeCurrentYear() {
        // Arrange
        Champion pastChampion = new Champion(currentYear - 1, "verstappen", "VER", "Max", "Verstappen", "Dutch", 454.0f, 15);
        List<Champion> allChampions = Arrays.asList(testChampion, pastChampion);
        
        SeasonInfo seasonInfo = SeasonInfo.builder()
            .year(currentYear)
            .isChampionAvailableForCurrentYear(false)
            .build();
        
        when(championRepository.findAll()).thenReturn(allChampions);
        when(seasonInfoRepository.findByYear(currentYear)).thenReturn(seasonInfo);

        // Act
        ResponseEntity<List<ChampionDTO>> response = championService.getChampions();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ChampionDTO> champions = response.getBody();
        assertEquals(1, champions.size());
        assertEquals(currentYear - 1, champions.get(0).getYear());
    }

    @Test
    void getChampions_WhenCurrentYearChampionAvailable_ShouldIncludeCurrentYear() {
        // Arrange
        Champion pastChampion = new Champion(currentYear - 1, "verstappen", "VER", "Max", "Verstappen", "Dutch", 454.0f, 15);
        List<Champion> allChampions = Arrays.asList(testChampion, pastChampion);
        
        SeasonInfo seasonInfo = SeasonInfo.builder()
            .year(currentYear)
            .isChampionAvailableForCurrentYear(true)
            .build();
        
        when(championRepository.findAll()).thenReturn(allChampions);
        when(seasonInfoRepository.findByYear(currentYear)).thenReturn(seasonInfo);

        // Act
        ResponseEntity<List<ChampionDTO>> response = championService.getChampions();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ChampionDTO> champions = response.getBody();
        assertEquals(2, champions.size());
        assertTrue(champions.stream().anyMatch(c -> c.getYear().equals(currentYear)));
    }

    @Test
    void getChampion_WhenNewChampionSaved_ShouldEvictAllChampionsCache() {
        // Arrange
        Champion champion = new Champion();
        champion.setYear(currentYear);
        champion.setDriverId("test_driver");
        
        SeasonInfo seasonInfo = SeasonInfo.builder()
            .year(currentYear)
            .isChampionAvailableForCurrentYear(true)
            .build();
        
        when(seasonInfoRepository.findByYear(currentYear)).thenReturn(seasonInfo);
        when(ergastApiService.fetchWorldChampion(eq(currentYear)))
            .thenReturn(ResponseEntity.ok(champion));
        when(championRepository.save(any(Champion.class))).thenReturn(champion);
        
        // Act
        ResponseEntity<ChampionDTO> result = championService.getChampion(currentYear);
        
        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(cacheService).evictChampionCache("allChampions");
    }

    @Test
    void getChampion_WhenExistingChampion_ShouldNotEvictCache() {
        // Arrange
        when(championRepository.findByYear(2023)).thenReturn(Optional.of(testChampion));

        // Act
        championService.getChampion(2023);

        // Assert
        verify(cacheService, never()).evictChampionCache(anyString());
    }

    @Test
    void getChampions_WhenCached_ShouldNotEvictCache() {
        // Arrange
        List<Champion> champions = Arrays.asList(testChampion);
        when(championRepository.findAll()).thenReturn(champions);
        when(seasonInfoRepository.findByYear(currentYear))
            .thenReturn(SeasonInfo.builder()
                .year(currentYear)
                .isChampionAvailableForCurrentYear(true)
                .build());

        // Act
        championService.getChampions();

        // Assert
        verify(cacheService, never()).evictChampionCache(anyString());
    }
}
