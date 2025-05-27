package com.f1.app.repository;

import com.f1.app.model.Race;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RaceRepository extends JpaRepository<Race, Long> {
    @Query("SELECT DISTINCT r FROM Race r LEFT JOIN FETCH r.results WHERE r.season = :season")
    List<Race> findBySeason(@Param("season") Integer season);
} 
