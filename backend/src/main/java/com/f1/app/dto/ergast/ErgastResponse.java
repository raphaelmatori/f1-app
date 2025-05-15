package com.f1.app.dto.ergast;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ErgastResponse {
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
        
        @JsonProperty("StandingsTable")
        private StandingsTable standingsTable;
        
        @JsonProperty("DriverTable")
        private DriverTable driverTable;
    }
    
    @Data
    public static class RaceTable {
        private String season;
        private String round;
        private Race[] Races;
    }
    
    @Data
    public static class StandingsTable {
        private String season;
        private StandingsList[] StandingsLists;
    }
    
    @Data
    public static class DriverTable {
        private String season;
        private Driver[] Drivers;
    }
    
    @Data
    public static class Race {
        private String season;
        private String round;
        private String url;
        private String raceName;
        private Circuit Circuit;
        private String date;
        private String time;
        private Results[] Results;
    }
    
    @Data
    public static class Circuit {
        private String circuitId;
        private String url;
        private String circuitName;
        private Location Location;
    }
    
    @Data
    public static class Location {
        private String lat;
        private String lng;
        private String locality;
        private String country;
    }
    
    @Data
    public static class Results {
        private String position;
        private String points;
        private Driver Driver;
        private Constructor Constructor;
        private String grid;
        private String laps;
        private String status;
        private Time Time;
        private FastestLap FastestLap;
    }
    
    @Data
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
    public static class Constructor {
        private String constructorId;
        private String url;
        private String name;
        private String nationality;
    }
    
    @Data
    public static class Time {
        private String millis;
        private String time;
    }
    
    @Data
    public static class FastestLap {
        private String rank;
        private String lap;
        private Time Time;
        private AverageSpeed AverageSpeed;
    }
    
    @Data
    public static class AverageSpeed {
        private String units;
        private String speed;
    }
    
    @Data
    public static class StandingsList {
        private String season;
        private String round;
        private DriverStanding[] DriverStandings;
    }
    
    @Data
    public static class DriverStanding {
        private String position;
        private String points;
        private String wins;
        private Driver Driver;
        private Constructor[] Constructors;
    }
} 