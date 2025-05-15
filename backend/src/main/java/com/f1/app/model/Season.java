package com.f1.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "seasons")
public class Season {
    
    @Id
    @Column(nullable = false)
    private Integer year;
    
    @ManyToOne
    @JoinColumn(name = "champion_id", nullable = false)
    private Driver champion;
    
    private Integer totalPoints;
    
    private Integer totalRaces;
    
    private Integer totalWins;
    
    private Integer totalPodiums;
    
    @Column(columnDefinition = "TEXT")
    private String notes;  // Any special notes about the championship
    
    @Column(unique = true)
    private String url;    // Wikipedia URL for the season
} 