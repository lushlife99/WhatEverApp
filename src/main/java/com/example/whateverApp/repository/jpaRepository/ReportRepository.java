package com.example.whateverApp.repository.jpaRepository;

import com.example.whateverApp.model.entity.Report;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import org.springframework.data.jpa.repository.JpaRepository;

import java.beans.JavaBean;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findByWorkAndReportUser(Work work, User user);
}
