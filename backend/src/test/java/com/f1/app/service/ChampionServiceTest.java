package com.f1.app.service;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.f1.app.dto.ChampionDTO;
import com.f1.app.exception.ServiceException;
import com.f1.app.model.Champion;
import com.f1.app.model.SeasonInfo;
import com.f1.app.repository.ChampionRepository;
import com.f1.app.repository.SeasonInfoRepository;

class ChampionServiceTest {

    @InjectMocks
    private ChampionService championService;

    @Mock
    private ChampionRepository championRepository;

    @Mock
    private SeasonInfoRepository seasonInfoRepository;

    @Mock
    private ErgastApiService ergastApiService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    private Champion testChampion;
    private ChampionDTO testChampionDTO;
    private final int currentYear = java.time.Year.now().getValue();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testChampion = new Champion(currentYear, "verstappen", "VER", "Max", "Verstappen", "Dutch", 454.0f, 19);
        testChampionDTO = ChampionDTO.fromEntity(testChampion);
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
    void initializeChampionData_ShouldAlwaysUpdateCurrentYear() {
        // Arrange
        Champion existingCurrentYearChampion = new Champion(currentYear, "hamilton", "HAM", "Lewis", "Hamilton", "British", 400.0f, 15);
        Champion updatedCurrentYearChampion = new Champion(currentYear, "verstappen", "VER", "Max", "Verstappen", "Dutch", 454.0f, 19);
        
        // Mock that we already have the current year champion in DB
        when(championRepository.findAll()).thenReturn(Arrays.asList(existingCurrentYearChampion));
        when(ergastApiService.fetchWorldChampion(currentYear)).thenReturn(ResponseEntity.ok(updatedCurrentYearChampion));
        when(championRepository.save(any(Champion.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        championService.initializeChampionData();

        // Assert
        verify(ergastApiService).fetchWorldChampion(currentYear);
        verify(championRepository).save(updatedCurrentYearChampion);
    }

    @Test
    void initializeChampionData_WhenCurrentYearFails_ShouldContinueWithPastYears() {
        // Arrange
        Champion pastYearChampion = new Champion(currentYear - 1, "verstappen", "VER", "Max", "Verstappen", "Dutch", 454.0f, 15);
        
        when(championRepository.findAll()).thenReturn(Collections.emptyList());
        when(ergastApiService.fetchWorldChampion(currentYear)).thenThrow(new RuntimeException("API Error"));
        when(ergastApiService.fetchWorldChampion(currentYear - 1)).thenReturn(ResponseEntity.ok(pastYearChampion));
        when(championRepository.save(any(Champion.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        championService.initializeChampionData();

        // Assert
        verify(ergastApiService).fetchWorldChampion(currentYear);
        verify(ergastApiService).fetchWorldChampion(currentYear - 1);
        verify(championRepository).save(pastYearChampion);
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
}
