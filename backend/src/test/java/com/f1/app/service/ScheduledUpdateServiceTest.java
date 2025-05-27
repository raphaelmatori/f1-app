package com.f1.app.service;

import java.time.LocalDate;
import java.time.Year;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.f1.app.model.Race;
import com.f1.app.model.SeasonInfo;
import com.f1.app.repository.SeasonInfoRepository;

@ExtendWith(MockitoExtension.class)
class ScheduledUpdateServiceTest {

    @Mock
    private ChampionService championService;

    @Mock
    private ErgastApiService ergastApiService;

    @Mock
    private SeasonInfoRepository seasonInfoRepository;

    @InjectMocks
    private ScheduledUpdateService scheduledUpdateService;

    private final int currentYear = Year.now().getValue();

    @Test
    void scheduledTasks_ShouldUpdateChampionsAndLastRaceInfo() {
        // Arrange
        Race lastRace = Race.builder()
            .season(currentYear)
            .round(22)
            .date("2023-12-31")
            .build();

        when(ergastApiService.fetchLastRaceOfSeason(currentYear))
            .thenReturn(Optional.of(lastRace));

        // Act
        scheduledUpdateService.scheduledTasks();

        // Assert
        verify(championService).initializeChampionData();
        verify(ergastApiService).fetchLastRaceOfSeason(currentYear);
        verify(seasonInfoRepository).save(any(SeasonInfo.class));
    }

    @Test
    void scheduledTasks_WhenNoLastRace_ShouldOnlyUpdateChampions() {
        // Arrange
        when(ergastApiService.fetchLastRaceOfSeason(currentYear))
            .thenReturn(Optional.empty());

        // Act
        scheduledUpdateService.scheduledTasks();

        // Assert
        verify(championService).initializeChampionData();
        verify(ergastApiService).fetchLastRaceOfSeason(currentYear);
        verify(seasonInfoRepository, never()).save(any(SeasonInfo.class));
    }

    @Test
    void scheduledTasks_WhenLastRaceInFuture_ShouldSetChampionNotAvailable() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusMonths(1);
        Race lastRace = Race.builder()
            .season(currentYear)
            .round(22)
            .date(futureDate.toString())
            .build();

        when(ergastApiService.fetchLastRaceOfSeason(currentYear))
            .thenReturn(Optional.of(lastRace));

        // Act
        scheduledUpdateService.scheduledTasks();

        // Assert
        verify(seasonInfoRepository).save(argThat(seasonInfo -> 
            !seasonInfo.isChampionAvailableForCurrentYear()));
    }

    @Test
    void scheduledTasks_WhenLastRaceInPast_ShouldSetChampionAvailable() {
        // Arrange
        LocalDate pastDate = LocalDate.now().minusMonths(1);
        Race lastRace = Race.builder()
            .season(currentYear)
            .round(22)
            .date(pastDate.toString())
            .build();

        when(ergastApiService.fetchLastRaceOfSeason(currentYear))
            .thenReturn(Optional.of(lastRace));

        // Act
        scheduledUpdateService.scheduledTasks();

        // Assert
        verify(seasonInfoRepository).save(argThat(seasonInfo -> 
            seasonInfo.isChampionAvailableForCurrentYear()));
    }
} 