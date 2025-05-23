package com.f1.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErgastChampionResponse {
    @JsonProperty("MRData")
    private MRData mrData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MRData {
        private String xmlns;
        private String series;
        private String url;
        private String limit;
        private String offset;
        private String total;
        @JsonProperty("StandingsTable")
        private StandingsTable standingsTable;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StandingsTable {
        private String driverStandings;
        private String season;
        private String round;
        @JsonProperty("StandingsLists")
        private StandingsList[] standingsLists;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StandingsList {
        private String season;
        private String round;
        @JsonProperty("DriverStandings")
        private DriverStanding[] driverStandings;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverStanding {
        private String position;
        private String positionText;
        private String points;
        private String wins;
        @JsonProperty("Driver")
        private Driver driver;
        @JsonProperty("Constructors")
        private Constructor[] constructors;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Driver {
        private String driverId;
        private String permanentNumber;
        private String code;
        private String url;
        private String givenName;
        private String familyName;
        private String dateOfBirth;
        private String nationality;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Constructor {
        private String constructorId;
        private String url;
        private String name;
        private String nationality;
    }
} 
