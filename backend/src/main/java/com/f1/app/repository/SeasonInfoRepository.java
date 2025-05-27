package com.f1.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.f1.app.model.SeasonInfo;

@Repository
public interface SeasonInfoRepository extends JpaRepository<SeasonInfo, Integer> {
    SeasonInfo findByYear(Integer year);
} 