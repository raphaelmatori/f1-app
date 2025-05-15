package com.f1.app.repository;

import com.f1.app.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByDriverId(String driverId);
    Optional<Driver> findByCode(String code);
} 