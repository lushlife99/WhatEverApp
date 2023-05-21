package com.example.whateverApp.controller;

import com.example.whateverApp.dto.ReportDto;
import com.example.whateverApp.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/report")
    public ReportDto createReport(@RequestBody ReportDto reportDto, HttpServletRequest request){
        return reportService.createReport(reportDto, request);
    }

}
