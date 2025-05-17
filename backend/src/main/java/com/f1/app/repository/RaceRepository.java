package com.f1.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.f1.app.model.Race;

@Repository
public interface RaceRepository extends JpaRepository<Race, Long> {
    List<Race> findBySeason(Integer season);
} 