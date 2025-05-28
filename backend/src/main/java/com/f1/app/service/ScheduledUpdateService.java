package com.f1.app.service;

import java.time.LocalDate;
import java.time.Year;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.f1.app.model.SeasonInfo;
import com.f1.app.repository.SeasonInfoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledUpdateService {

    private final ChampionService championService;
    private final ErgastApiService ergastApiService;
    private final SeasonInfoRepository seasonInfoRepository;
    private final RaceService raceService;

    @Scheduled(cron = "0 0 0 * * 1") // Run at midnight every Monday (0 0 0 = midnight, 1 = Monday)
    public void scheduledTasks() {
        log.info("Running weekly scheduled tasks");
        
        // Only evict cache for current year as that's the only data that might change
        int currentYear = Year.now().getValue();
        raceService.evictRaceCache(currentYear);
        
        updateChampions();
        updateLastRaceInfo();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        scheduledTasks();
    }

    private void updateChampions() {
        log.info("Updating champions data");
        championService.initializeChampionData();
    }

    private void updateLastRaceInfo() {
        log.info("Updating last race information");
        int currentYear = Year.now().getValue();
        
        ergastApiService.fetchLastRaceOfSeason(currentYear)
            .ifPresent(lastRace -> {
                LocalDate lastRaceDate = LocalDate.parse(lastRace.getDate());
                SeasonInfo seasonInfo = SeasonInfo.builder()
                    .year(currentYear)
                    .lastRoundNumber(lastRace.getRound())
                    .lastRaceDate(lastRaceDate)
                    .isChampionAvailableForCurrentYear(LocalDate.now().isAfter(lastRaceDate))
                    .build();
                
                seasonInfoRepository.save(seasonInfo);
                
                // Cache is already evicted at the start of scheduledTasks
                
                log.info("Updated season info for year: {}", currentYear);
            });
    }
} 
