package com.f1.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
