package com.f1.app.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "race")
@EqualsAndHashCode(exclude = "race")
@Table(name = "race_results")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RaceResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String position;
    private String points;
    private String grid;
    private String laps;
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "race_id")
    @JsonBackReference
    private Race race;

    @Embedded
    private Driver driver;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "constructorId", column = @Column(name = "constructor_id")),
            @AttributeOverride(name = "name", column = @Column(name = "constructor_name")),
            @AttributeOverride(name = "nationality", column = @Column(name = "constructor_nationality"))
    })
    private Constructor constructor;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "millis", column = @Column(name = "time_millis")),
            @AttributeOverride(name = "time", column = @Column(name = "time_value"))
    })
    private RaceTime time;

    public void setRace(Race race) {
        this.race = race;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Embeddable
    public static class Driver implements Serializable {
        private static final long serialVersionUID = 1L;

        private String driverId;
        private String code;
        private String givenName;
        private String familyName;
        private String nationality;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Embeddable
    public static class Constructor implements Serializable {
        private static final long serialVersionUID = 1L;

        private String constructorId;
        private String name;
        private String nationality;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Embeddable
    public static class RaceTime implements Serializable {
        private static final long serialVersionUID = 1L;

        private String millis;
        private String time;
    }
} 
