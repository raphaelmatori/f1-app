package com.f1.app.model;

import java.io.Serializable;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "race_results")
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
    
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
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
    public static class RaceTime implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String millis;
        private String time;
    }
} 