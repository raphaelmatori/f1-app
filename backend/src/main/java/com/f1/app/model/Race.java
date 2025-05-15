package com.f1.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "races")
public class Race {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer season;
    
    @Column(nullable = false)
    private Integer round;
    
    @Column(nullable = false)
    private String raceName;
    
    @Column(nullable = false)
    private String circuitId;
    
    @Column(nullable = false)
    private String circuitName;
    
    private String locality;
    
    private String country;
    
    @Column(nullable = false)
    private LocalDateTime date;
    
    @Column(unique = true)
    private String url;  // Wikipedia URL
    
    @ManyToOne
    @JoinColumn(name = "winner_id")
    private Driver winner;
    
    private String winnerTime;  // Race winning time
    
    private Integer laps;       // Total race laps
    
    @Column(columnDefinition = "TEXT")
    private String notes;       // Any special notes about the race
} 