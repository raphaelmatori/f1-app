package com.f1.app.repository;

import com.f1.app.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Integer> {
    List<Season> findByChampion_DriverId(String driverId);
    List<Season> findAllByOrderByYearDesc();
} 