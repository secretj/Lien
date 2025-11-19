package com.lien.repository;

import com.lien.entity.GoogleMapsConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GoogleMapsConfigRepository extends JpaRepository<GoogleMapsConfig, Long> {
    Optional<GoogleMapsConfig> findFirstByOrderByIdDesc();
}