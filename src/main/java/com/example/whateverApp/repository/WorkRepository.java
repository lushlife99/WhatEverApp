package com.example.whateverApp.repository;

import com.example.whateverApp.model.entity.Work;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkRepository extends JpaRepository<Work, Long> {
}
