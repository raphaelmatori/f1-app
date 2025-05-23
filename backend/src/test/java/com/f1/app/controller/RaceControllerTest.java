package com.f1.app.controller;

import com.f1.app.dto.RaceDTO;
import com.f1.app.model.Race;
import com.f1.app.service.RaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RaceControllerTest {

    @Mock
    private RaceService raceService;

    @InjectMocks
    private RaceController raceController;

    private MockMvc mockMvc;
    private final int TEST_YEAR = 2023;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(raceController).build();
    }

    @Test
    void getRacesByYear_WhenRacesExist_ReturnsOk() throws Exception {
        // Arrange
        List<RaceDTO> races = createTestRaces();
        when(raceService.getRacesByYear(TEST_YEAR)).thenReturn(races);

        // Act & Assert
        mockMvc.perform(get("/api/v1/races/{year}", TEST_YEAR))
            .andExpect(status().isOk());
    }

    @Test
    void getRacesByYear_WhenNoRaces_ReturnsNotFound() throws Exception {
        // Arrange
        when(raceService.getRacesByYear(anyInt())).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/v1/races/{year}", TEST_YEAR))
            .andExpect(status().isNotFound());
    }

    private List<RaceDTO> createTestRaces() {
        List<RaceDTO> races = new ArrayList<>();
        RaceDTO race = RaceDTO.fromEntity(Race.builder()
            .season(TEST_YEAR)
            .round(1)
            .raceName("Test Grand Prix")
            .date("2023-03-05")
            .circuit(Race.Circuit.builder()
                .circuitId("test_circuit")
                .circuitName("Test Circuit")
                .locality("Test City")
                .country("Test Country")
                .build())
            .build());
        races.add(race);
        return races;
    }
} 
