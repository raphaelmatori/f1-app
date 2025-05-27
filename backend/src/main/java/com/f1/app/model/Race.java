package com.f1.app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "results")
@EqualsAndHashCode(exclude = "results")
@Table(name = "races")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    @OneToMany(mappedBy = "race", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    @JsonManagedReference
    private List<RaceResult> results = new ArrayList<>();

    public void addResult(RaceResult result) {
        if (results == null) {
            results = new ArrayList<>();
        }
        results.add(result);
        result.setRace(this);
    }

    public void setResults(List<RaceResult> results) {
        if (this.results == null) {
            this.results = new ArrayList<>();
        }
        this.results.clear();
        if (results != null) {
            results.forEach(this::addResult);
        }
    }

    @PrePersist
    @PreUpdate
    private void initializeResults() {
        if (results == null) {
            results = new ArrayList<>();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Embeddable
    public static class Circuit implements Serializable {
        private static final long serialVersionUID = 1L;

        private String circuitId;
        private String circuitName;
        private String locality;
        private String country;
    }
} 
