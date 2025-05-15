package com.f1.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeasonDTO {
    private Integer year;
    private DriverDTO champion;
    private Integer totalPoints;
    private Integer totalRaces;
    private Integer totalWins;
    private Integer totalPodiums;
    private String notes;
    private String url;
} 