package com.f1.app.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Driver implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String driverId;
        private String code;
        private String givenName;
        private String familyName;
        private String nationality;
    }
    
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Constructor implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String constructorId;
        private String name;
        private String nationality;
    }
    
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RaceTime implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String millis;
        private String time;
    }
} 