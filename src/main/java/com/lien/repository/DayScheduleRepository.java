package com.lien.repository;

import com.lien.entity.DaySchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DayScheduleRepository extends JpaRepository<DaySchedule, Long> {
}