package com.f1.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaceDTO {
    private Long id;
    private Integer season;
    private Integer round;
    private String raceName;
    private String circuitId;
    private String circuitName;
    private String locality;
    private String country;
    private LocalDateTime date;
    private String url;
    private DriverDTO winner;
    private String winnerTime;
    private Integer laps;
    private String notes;
} 