package com.example.whateverApp.repository;

import com.example.whateverApp.model.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
}
