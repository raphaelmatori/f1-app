package com.f1.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "drivers")
public class Driver {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String driverId;  // Ergast API driver ID
    
    @Column(nullable = false)
    private String code;      // Driver's short code (e.g., HAM, VER)
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(nullable = false)
    private String dateOfBirth;
    
    private String nationality;
    
    @Column(unique = true)
    private String url;       // Wikipedia URL
    
    private String permanentNumber;  // Driver's chosen number
} 