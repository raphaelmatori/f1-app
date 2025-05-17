package com.f1.app.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ErgastRaceResponse {
    @JsonProperty("MRData")
    private MRData mrData;

    @Data
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
    public static class RaceTable {
        private String season;
        private String round;
        
        @JsonProperty("Races")
        private List<RaceData> races;
    }

    @Data
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
    public static class CircuitData {
        private String circuitId;
        private String url;
        private String circuitName;
        
        @JsonProperty("Location")
        private LocationData location;
    }

    @Data
    public static class LocationData {
        private String lat;
        private String lang;
        private String locality;
        private String country;
    }

    @Data
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
    public static class DriverData {
        private String driverId;
        private String code;
        private String url;
        private String givenName;
        private String familyName;
        private String dateOfBirth;
        private String nationality;
    }

    @Data
    public static class ConstructorData {
        private String constructorId;
        private String url;
        private String name;
        private String nationality;
    }

    @Data
    public static class TimeData {
        private String millis;
        private String time;
    }
} 