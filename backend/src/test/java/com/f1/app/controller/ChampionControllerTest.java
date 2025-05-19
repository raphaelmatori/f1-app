package com.f1.app.controller;

import com.f1.app.model.Champion;
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

    @Mock
    private ChampionService championService;

    @InjectMocks
    private ChampionController championController;

    private MockMvc mockMvc;
    private final int TEST_YEAR = 2023;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(championController).build();
    }

    @Test
    void getChampion_WhenChampionExists_ReturnsOk() throws Exception {
        // Arrange
        Champion champion = createTestChampion();
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

    private Champion createTestChampion() {
        return new Champion(
            TEST_YEAR,
            "max_verstappen",
            "VER",
            "Max",
            "Verstappen",
            "Dutch",
            454.0f,
            19
        );
    }
}
