package com.f1.app.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "season_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeasonInfo {
    @Id
    private Integer year;
    private Integer lastRoundNumber;
    private LocalDate lastRaceDate;
    private boolean isChampionAvailableForCurrentYear;
} 