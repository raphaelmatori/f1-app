package com.f1.app.controller;

import com.f1.app.dto.ChampionDTO;
import com.f1.app.service.ChampionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChampionControllerTest {

    private final int TEST_YEAR = 2023;
    @Mock
    private ChampionService championService;
    @InjectMocks
    private ChampionController championController;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(championController).build();
    }

    @Test
    void getChampion_WhenChampionExists_ReturnsOk() throws Exception {
        // Arrange
        ChampionDTO champion = createTestChampion();
        when(championService.getChampion(TEST_YEAR)).thenReturn(ResponseEntity.ok(champion));

        // Act & Assert
        mockMvc.perform(get("/api/v1/champions/{year}", TEST_YEAR))
                .andExpect(status().isOk());
    }

    @Test
    void getChampion_WhenNoChampion_ReturnsNotFound() throws Exception {
        // Arrange
        when(championService.getChampion(anyInt())).thenReturn(ResponseEntity.notFound().build());

        // Act & Assert
        mockMvc.perform(get("/api/v1/champions/{year}", TEST_YEAR))
                .andExpect(status().isNotFound());
    }

    private ChampionDTO createTestChampion() {
        return ChampionDTO.builder()
                .year(TEST_YEAR)
                .driverId("max_verstappen")
                .code("VER")
                .givenName("Max")
                .familyName("Verstappen")
                .nationality("Dutch")
                .points(454.0f)
                .wins(19)
                .build();
    }
}
