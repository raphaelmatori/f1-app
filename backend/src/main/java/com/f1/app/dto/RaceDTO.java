package com.f1.app.dto;

import com.f1.app.model.Race;
import com.f1.app.model.RaceResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer season;
    private Integer round;
    private String raceName;
    private String date;
    private String time;
    private CircuitDTO circuit;
    @Builder.Default
    private List<RaceResultDTO> results = new ArrayList<>();

    public static RaceDTO fromEntity(Race race) {
        if (race == null) return null;

        RaceDTO.RaceDTOBuilder builder = RaceDTO.builder()
                .season(race.getSeason())
                .round(race.getRound())
                .raceName(race.getRaceName())
                .date(race.getDate())
                .time(race.getTime());

        if (race.getCircuit() != null) {
            CircuitDTO circuitDTO = CircuitDTO.builder()
                    .circuitId(race.getCircuit().getCircuitId())
                    .circuitName(race.getCircuit().getCircuitName())
                    .locality(race.getCircuit().getLocality())
                    .country(race.getCircuit().getCountry())
                    .build();
            builder.circuit(circuitDTO);
        }

        RaceDTO dto = builder.build();

        if (race.getResults() != null) {
            for (RaceResult result : race.getResults()) {
                if (result != null) {
                    dto.getResults().add(RaceResultDTO.fromEntity(result));
                }
            }
        }

        return dto;
    }

    public Race toEntity() {
        Race.RaceBuilder builder = Race.builder()
                .season(this.season)
                .round(this.round)
                .raceName(this.raceName)
                .date(this.date)
                .time(this.time);

        if (this.circuit != null) {
            Race.Circuit circuit = Race.Circuit.builder()
                    .circuitId(this.circuit.getCircuitId())
                    .circuitName(this.circuit.getCircuitName())
                    .locality(this.circuit.getLocality())
                    .country(this.circuit.getCountry())
                    .build();
            builder.circuit(circuit);
        }

        Race race = builder.build();

        if (this.results != null) {
            for (RaceResultDTO resultDTO : this.results) {
                if (resultDTO != null) {
                    RaceResult result = resultDTO.toEntity();
                    result.setRace(race);
                    race.addResult(result);
                }
            }
        }

        return race;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CircuitDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String circuitId;
        private String circuitName;
        private String locality;
        private String country;
    }
} 
