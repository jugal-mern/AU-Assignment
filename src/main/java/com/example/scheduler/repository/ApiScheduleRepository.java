package com.example.scheduler.repository;

import com.example.scheduler.entity.ApiSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiScheduleRepository extends JpaRepository<ApiSchedule, Long> {
}
