package com.f1.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverDTO {
    private String driverId;
    private String code;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String nationality;
    private String url;
    private String permanentNumber;
    private Integer championships;    // Number of championships won
    private Integer totalRaceWins;    // Total number of race wins
    private Integer totalPodiums;     // Total number of podium finishes
} 