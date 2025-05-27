package com.f1.app.dto;

import com.f1.app.model.Champion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChampionDTOTest {

    private static final int TEST_YEAR = 2023;
    private static final String DRIVER_ID = "max_verstappen";
    private static final String CODE = "VER";
    private static final String GIVEN_NAME = "Max";
    private static final String FAMILY_NAME = "Verstappen";
    private static final String NATIONALITY = "Dutch";
    private static final float POINTS = 454.0f;
    private static final int WINS = 19;

    @Test
    void fromEntity_WhenValidEntity_CreatesMatchingDTO() {
        // Arrange
        Champion champion = new Champion(
                TEST_YEAR,
                DRIVER_ID,
                CODE,
                GIVEN_NAME,
                FAMILY_NAME,
                NATIONALITY,
                POINTS,
                WINS
        );

        // Act
        ChampionDTO dto = ChampionDTO.fromEntity(champion);

        // Assert
        assertNotNull(dto);
        assertEquals(TEST_YEAR, dto.getYear());
        assertEquals(DRIVER_ID, dto.getDriverId());
        assertEquals(CODE, dto.getCode());
        assertEquals(GIVEN_NAME, dto.getGivenName());
        assertEquals(FAMILY_NAME, dto.getFamilyName());
        assertEquals(NATIONALITY, dto.getNationality());
        assertEquals(POINTS, dto.getPoints());
        assertEquals(WINS, dto.getWins());
    }

    @Test
    void fromEntity_WhenNullEntity_ReturnsNull() {
        assertNull(ChampionDTO.fromEntity(null));
    }

    @Test
    void builder_CreatesValidDTO() {
        // Act
        ChampionDTO dto = ChampionDTO.builder()
                .year(TEST_YEAR)
                .driverId(DRIVER_ID)
                .code(CODE)
                .givenName(GIVEN_NAME)
                .familyName(FAMILY_NAME)
                .nationality(NATIONALITY)
                .points(POINTS)
                .wins(WINS)
                .build();

        // Assert
        assertNotNull(dto);
        assertEquals(TEST_YEAR, dto.getYear());
        assertEquals(DRIVER_ID, dto.getDriverId());
        assertEquals(CODE, dto.getCode());
        assertEquals(GIVEN_NAME, dto.getGivenName());
        assertEquals(FAMILY_NAME, dto.getFamilyName());
        assertEquals(NATIONALITY, dto.getNationality());
        assertEquals(POINTS, dto.getPoints());
        assertEquals(WINS, dto.getWins());
    }
} 
