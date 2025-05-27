package com.f1.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErgastRaceResponse {
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

        @JsonProperty("RaceTable")
        private RaceTable raceTable;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RaceTable {
        private String season;
        private String round;

        @JsonProperty("Races")
        private List<RaceData> races;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RaceData {
        private String season;
        private String round;
        private String url;

        @JsonProperty("raceName")
        private String raceName;

        private String date;
        private String time;

        @JsonProperty("Circuit")
        private CircuitData circuit;

        @JsonProperty("Results")
        private List<ResultData> results;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CircuitData {
        @JsonProperty("circuitId")
        private String circuitId;

        private String url;

        @JsonProperty("circuitName")
        private String circuitName;

        @JsonProperty("Location")
        private LocationData location;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationData {
        private String lat;
        private String lang;
        private String locality;
        private String country;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultData {
        private String number;
        private String position;
        private String positionText;
        private String points;
        private String grid;
        private String laps;
        private String status;

        @JsonProperty("Driver")
        private DriverData driver;

        @JsonProperty("Constructor")
        private ConstructorData constructor;

        @JsonProperty("Time")
        private TimeData time;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverData {
        @JsonProperty("driverId")
        private String driverId;

        private String code;
        private String url;

        @JsonProperty("givenName")
        private String givenName;

        @JsonProperty("familyName")
        private String familyName;

        @JsonProperty("dateOfBirth")
        private String dateOfBirth;

        private String nationality;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConstructorData {
        @JsonProperty("constructorId")
        private String constructorId;

        private String url;
        private String name;
        private String nationality;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeData {
        private String millis;
        private String time;
    }
} 
