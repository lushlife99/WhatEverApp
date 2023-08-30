package com.example.whateverApp.controller;

import com.example.whateverApp.dto.ReportDto;
import com.example.whateverApp.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    @PostMapping()
    public ReportDto createReport(@RequestBody ReportDto reportDto, HttpServletRequest request){
        return reportService.createReport(reportDto, request);
    }

    @GetMapping("/reportList")
    public List<ReportDto> getMyReportList(HttpServletRequest request){
        return reportService.getMyReportList(request);
    }

    @DeleteMapping("/{reportId}")
    public List<ReportDto> deleteReport(@PathVariable Long reportId, HttpServletRequest request){
        reportService.deleteReport(reportId, request);
        return reportService.getMyReportList(request);
    }

    @PutMapping("")
    public List<ReportDto> modifyReport(@RequestBody ReportDto reportDto, HttpServletRequest request){
        reportService.modifyReport(reportDto, request);
        return reportService.getMyReportList(request);
    }

}
