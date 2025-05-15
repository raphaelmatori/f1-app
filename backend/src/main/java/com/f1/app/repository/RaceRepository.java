package com.f1.app.repository;

import com.f1.app.model.Race;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RaceRepository extends JpaRepository<Race, Long> {
    List<Race> findBySeasonOrderByRoundAsc(Integer season);
    List<Race> findBySeasonAndWinner_DriverId(Integer season, String driverId);
} 