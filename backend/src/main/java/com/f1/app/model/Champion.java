package com.f1.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "champions")
public class Champion {
    
    @Id
    private Integer year;
    private String driverId;
    private String code;
    private String givenName;
    private String familyName;
    private String nationality;
    private Float points;
    private Integer wins;
} 
