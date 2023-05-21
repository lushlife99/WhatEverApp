package com.example.whateverApp.repository;

import com.example.whateverApp.model.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.beans.JavaBean;

public interface ReportRepository extends JpaRepository<Report, Long> {

}
