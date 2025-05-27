package com.f1.app.dto;

import com.f1.app.model.Champion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChampionDTO {
    private Integer year;
    private String driverId;
    private String code;
    private String givenName;
    private String familyName;
    private String nationality;
    private Float points;
    private Integer wins;

    public static ChampionDTO fromEntity(Champion champion) {
        if (champion == null) {
            return null;
        }

        return ChampionDTO.builder()
                .year(champion.getYear())
                .driverId(champion.getDriverId())
                .code(champion.getCode())
                .givenName(champion.getGivenName())
                .familyName(champion.getFamilyName())
                .nationality(champion.getNationality())
                .points(champion.getPoints())
                .wins(champion.getWins())
                .build();
    }
} 