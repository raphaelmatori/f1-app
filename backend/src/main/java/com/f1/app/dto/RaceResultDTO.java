package com.f1.app.dto;

import com.f1.app.model.RaceResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaceResultDTO {
    private String position;
    private String points;
    private String grid;
    private String laps;
    private String status;
    private DriverDTO driver;
    private ConstructorDTO constructor;
    private RaceTimeDTO time;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverDTO {
        private String driverId;
        private String code;
        private String givenName;
        private String familyName;
        private String nationality;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConstructorDTO {
        private String constructorId;
        private String name;
        private String nationality;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RaceTimeDTO {
        private String millis;
        private String time;
    }

    public static RaceResultDTO fromEntity(RaceResult result) {
        if (result == null) return null;

        RaceResultDTO.RaceResultDTOBuilder builder = RaceResultDTO.builder()
            .position(result.getPosition())
            .points(result.getPoints())
            .grid(result.getGrid())
            .laps(result.getLaps())
            .status(result.getStatus());

        if (result.getDriver() != null) {
            DriverDTO driverDTO = DriverDTO.builder()
                .driverId(result.getDriver().getDriverId())
                .code(result.getDriver().getCode())
                .givenName(result.getDriver().getGivenName())
                .familyName(result.getDriver().getFamilyName())
                .nationality(result.getDriver().getNationality())
                .build();
            builder.driver(driverDTO);
        }

        if (result.getConstructor() != null) {
            ConstructorDTO constructorDTO = ConstructorDTO.builder()
                .constructorId(result.getConstructor().getConstructorId())
                .name(result.getConstructor().getName())
                .nationality(result.getConstructor().getNationality())
                .build();
            builder.constructor(constructorDTO);
        }

        if (result.getTime() != null) {
            RaceTimeDTO timeDTO = RaceTimeDTO.builder()
                .millis(result.getTime().getMillis())
                .time(result.getTime().getTime())
                .build();
            builder.time(timeDTO);
        }

        return builder.build();
    }

    public RaceResult toEntity() {
        RaceResult.RaceResultBuilder builder = RaceResult.builder()
            .position(this.position)
            .points(this.points)
            .grid(this.grid)
            .laps(this.laps)
            .status(this.status);

        if (this.driver != null) {
            RaceResult.Driver driver = RaceResult.Driver.builder()
                .driverId(this.driver.getDriverId())
                .code(this.driver.getCode())
                .givenName(this.driver.getGivenName())
                .familyName(this.driver.getFamilyName())
                .nationality(this.driver.getNationality())
                .build();
            builder.driver(driver);
        }

        if (this.constructor != null) {
            RaceResult.Constructor constructor = RaceResult.Constructor.builder()
                .constructorId(this.constructor.getConstructorId())
                .name(this.constructor.getName())
                .nationality(this.constructor.getNationality())
                .build();
            builder.constructor(constructor);
        }

        if (this.time != null) {
            RaceResult.RaceTime time = RaceResult.RaceTime.builder()
                .millis(this.time.getMillis())
                .time(this.time.getTime())
                .build();
            builder.time(time);
        }

        return builder.build();
    }
} 
