package com.f1.app.service;

import com.f1.app.exception.ServiceException;
import com.f1.app.model.Champion;
import com.f1.app.repository.ChampionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChampionServiceTest {

    @InjectMocks
    private ChampionService championService;

    @Mock
    private ChampionRepository championRepository;

    @Mock
    private ErgastApiService ergastApiService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    private Champion testChampion;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testChampion = new Champion(2023, "verstappen", "VER", "Max", "Verstappen", "Dutch", 454.0f, 19);
    }

    @Test
    void getChampions_ShouldReturnAllChampions() {
        List<Champion> expectedChampions = Arrays.asList(
                testChampion,
                new Champion(2022, "verstappen", "VER", "Max", "Verstappen", "Dutch", 454.0f, 15)
        );
        when(championRepository.findAll()).thenReturn(expectedChampions);

        ResponseEntity<List<Champion>> response = championService.getChampions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedChampions, response.getBody());
        verify(championRepository).findAll();
    }

    @Test
    void getChampions_WhenRepositoryThrowsException_ShouldThrowServiceException() {
        when(championRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        ServiceException exception = assertThrows(ServiceException.class, () -> championService.getChampions());
        assertEquals("Failed to fetch champions", exception.getMessage());
        assertEquals("CHAMPIONS_FETCH_ERROR", exception.getCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getStatus());
    }

    @Test
    void getChampion_WhenExistsInDatabase_ShouldReturnChampion() {
        when(championRepository.findByYear(2023)).thenReturn(Optional.of(testChampion));

        ResponseEntity<Champion> response = championService.getChampion(2023);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testChampion, response.getBody());
        verify(championRepository).findByYear(2023);
        verify(ergastApiService, never()).fetchWorldChampion(anyInt());
    }

    @Test
    void getChampion_WhenNotInDatabase_ShouldFetchFromApi() {
        when(championRepository.findByYear(2023)).thenReturn(Optional.empty());
        when(ergastApiService.fetchWorldChampion(2023)).thenReturn(ResponseEntity.ok(testChampion));
        when(championRepository.save(testChampion)).thenReturn(testChampion);

        ResponseEntity<Champion> response = championService.getChampion(2023);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testChampion, response.getBody());
        verify(championRepository).findByYear(2023);
        verify(ergastApiService).fetchWorldChampion(2023);
        verify(championRepository).save(testChampion);
    }

    @Test
    void getChampion_WhenApiReturnsError_ShouldThrowServiceException() {
        when(championRepository.findByYear(2023)).thenReturn(Optional.empty());
        when(ergastApiService.fetchWorldChampion(2023)).thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

        ServiceException exception = assertThrows(ServiceException.class, () -> championService.getChampion(2023));
        assertEquals("Failed to fetch champion data from external API", exception.getMessage());
        assertEquals("CHAMPION_API_ERROR", exception.getCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), exception.getStatus());
    }

    @Test
    void getChampion_WhenSaveFails_ShouldStillReturnChampion() {
        when(championRepository.findByYear(2023)).thenReturn(Optional.empty());
        when(ergastApiService.fetchWorldChampion(2023)).thenReturn(ResponseEntity.ok(testChampion));
        when(championRepository.save(testChampion)).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<Champion> response = championService.getChampion(2023);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testChampion, response.getBody());
        verify(championRepository).findByYear(2023);
        verify(ergastApiService).fetchWorldChampion(2023);
        verify(championRepository).save(testChampion);
    }

    @Test
    void initializeChampionData_ShouldFetchAndSaveMissingChampions() {
        int currentYear = java.time.Year.now().getValue();
        Champion champion1 = new Champion(currentYear, "verstappen", "VER", "Max", "Verstappen", "Dutch", 454.0f, 19);
        Champion champion2 = new Champion(currentYear - 1, "verstappen", "VER", "Max", "Verstappen", "Dutch", 454.0f, 15);

        when(championRepository.findAll()).thenReturn(Collections.emptyList());
        when(ergastApiService.fetchWorldChampion(currentYear)).thenReturn(ResponseEntity.ok(champion1));
        when(ergastApiService.fetchWorldChampion(currentYear - 1)).thenReturn(ResponseEntity.ok(champion2));
        when(championRepository.save(any(Champion.class))).thenAnswer(i -> i.getArgument(0));

        championService.initializeChampionData();

        verify(championRepository).findAll();
        verify(ergastApiService).fetchWorldChampion(currentYear);
        verify(ergastApiService).fetchWorldChampion(currentYear - 1);
        verify(championRepository, times(2)).save(any(Champion.class));
    }

    @Test
    void initializeChampionData_WhenRepositoryThrowsException_ShouldThrowServiceException() {
        when(championRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        ServiceException exception = assertThrows(ServiceException.class, () -> championService.initializeChampionData());
        assertEquals("Failed to initialize champion data", exception.getMessage());
        assertEquals("CHAMPION_INIT_ERROR", exception.getCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getStatus());
    }
}
