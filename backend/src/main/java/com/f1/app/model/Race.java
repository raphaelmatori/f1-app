package com.f1.app.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
@Table(name = "races")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Race implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Integer season;
    private Integer round;
    private String raceName;
    private String date;
    private String time;
    
    @Embedded
    private Circuit circuit;
    
    @OneToMany(mappedBy = "race", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    private List<RaceResult> results = new ArrayList<>();
    
    public void addResult(RaceResult result) {
        if (results == null) {
            results = new ArrayList<>();
        }
        results.add(result);
        result.setRace(this);
    }
    
    @PrePersist
    @PreUpdate
    private void initializeResults() {
        if (results == null) {
            results = new ArrayList<>();
        }
    }
    
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Circuit implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String circuitId;
        private String circuitName;
        private String locality;
        private String country;
    }
} 