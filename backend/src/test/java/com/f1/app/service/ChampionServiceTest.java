package com.f1.app.service;

import com.f1.app.model.Champion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.cache.Cache;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class ChampionServiceTest {
    @Mock
    private RedisCacheManager redisCacheManager;
    @Mock
    private ErgastApiService ergastApiService;
    @Mock
    private Cache redisCache;
    @InjectMocks
    private ChampionService championService;
    private final int TEST_YEAR = 2023;
    private Champion testChampion;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testChampion = new Champion(TEST_YEAR, "id", "code", "Max", "Verstappen", "Dutch", 454.0f, 19);
        when(redisCacheManager.getCache(anyString())).thenReturn(redisCache);
    }

    @Test
    void getChampion_WhenInRedisCache_ReturnsFromCache() {
        when(redisCache.get(TEST_YEAR)).thenReturn(() -> testChampion);
        ResponseEntity<Champion> result = championService.getChampion(TEST_YEAR);
        assertTrue(result.getStatusCode().is2xxSuccessful());
        assertNotNull(result.getBody());
        assertEquals(testChampion, result.getBody());
        verify(ergastApiService, never()).fetchWorldChampion(anyInt());
    }

    @Test
    void getChampion_WhenNotInCache_FetchesFromAPIAndCaches() {
        when(redisCache.get(TEST_YEAR)).thenReturn(null);
        when(ergastApiService.fetchWorldChampion(TEST_YEAR)).thenReturn(ResponseEntity.ok(testChampion));
        ResponseEntity<Champion> result = championService.getChampion(TEST_YEAR);
        assertTrue(result.getStatusCode().is2xxSuccessful());
        assertNotNull(result.getBody());
        assertEquals(testChampion, result.getBody());
        verify(redisCache).put(eq(TEST_YEAR), eq(testChampion));
    }

    @Test
    void getChampion_WhenNotInCache_AndApiReturnsNotFound_ReturnsNotFound() {
        when(redisCache.get(TEST_YEAR)).thenReturn(null);
        when(ergastApiService.fetchWorldChampion(TEST_YEAR)).thenReturn(ResponseEntity.notFound().build());
        ResponseEntity<Champion> result = championService.getChampion(TEST_YEAR);
        assertTrue(result.getStatusCode().is4xxClientError());
        verify(redisCache, never()).put(anyInt(), any());
    }

    @Test
    void getChampion_WhenRedisCacheThrowsException_ContinuesWithApi() {
        when(redisCache.get(TEST_YEAR)).thenThrow(new RuntimeException("Cache error"));
        when(ergastApiService.fetchWorldChampion(TEST_YEAR)).thenReturn(ResponseEntity.ok(testChampion));
        ResponseEntity<Champion> result = championService.getChampion(TEST_YEAR);
        assertTrue(result.getStatusCode().is2xxSuccessful());
        assertNotNull(result.getBody());
        assertEquals(testChampion, result.getBody());
    }

    @Test
    void getChampion_WhenPutInRedisCacheThrowsException_StillReturnsApiResult() {
        when(redisCache.get(TEST_YEAR)).thenReturn(null);
        when(ergastApiService.fetchWorldChampion(TEST_YEAR)).thenReturn(ResponseEntity.ok(testChampion));
        doThrow(new RuntimeException("Put error")).when(redisCache).put(eq(TEST_YEAR), eq(testChampion));
        ResponseEntity<Champion> result = championService.getChampion(TEST_YEAR);
        assertTrue(result.getStatusCode().is2xxSuccessful());
        assertNotNull(result.getBody());
        assertEquals(testChampion, result.getBody());
    }
}
