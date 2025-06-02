package com.f1.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.f1.app.model.Race;

@Repository
public interface RaceRepository extends JpaRepository<Race, Long> {
    @Query("SELECT DISTINCT r FROM Race r LEFT JOIN FETCH r.results WHERE r.season = :season")
    List<Race> findBySeason(@Param("season") Integer season);

    @Query("SELECT DISTINCT r FROM Race r LEFT JOIN FETCH r.results WHERE r.season = :season AND r.round = :round")
    Optional<Race> findBySeasonAndRound(@Param("season") Integer season, @Param("round") Integer round);
} 
