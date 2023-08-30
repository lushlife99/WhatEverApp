package com.example.whateverApp.controller;


import com.example.whateverApp.dto.*;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.el.parser.Token;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;


    @PostMapping("/loginAdmin")
    public TokenInfo adminLogin(@RequestBody User user, HttpServletResponse response){
        return adminService.login(user, response);
    }

    @GetMapping("/api/admin/conversation/{conversationId}")
    public ConversationDto getConv(@PathVariable String conversationId, HttpServletRequest request){
        return adminService.getConv(conversationId, request);
    }

    @GetMapping("/api/admin/work/{workId}")
    public WorkDto getWork(@PathVariable Long workId, HttpServletRequest request){
        return adminService.getWork(workId, request);
    }

    @GetMapping("/api/admin/user/{userId}")
    public UserDto getUserInfo(@PathVariable Long userId, HttpServletRequest request){
        return adminService.getUserInfo(userId, request);
    }


    @GetMapping("/api/admin/punishReportList/{userId}")
    public List<ReportDto> userPunishReportList(@PathVariable Long userId, HttpServletRequest request){
        return adminService.getUserPunishList(userId, request);
    }


    @PutMapping("/api/admin/report/execute")
    public ReportDto executeReport(@RequestBody ReportDto reportDto, HttpServletRequest request){
        adminService.executeReport(reportDto, request);
    }

    @GetMapping("/api/admin/reportList/writeByHelper")
    public List<ReportDto> getReportListWriteByHelper(HttpServletRequest request){
        return adminService.getReportListWriteByHelper(request);
    }

    @GetMapping("/api/admin/reportList/writeByCustomer")
    public List<ReportDto> getReportListWriteByCustomer(HttpServletRequest request){
        return adminService.getReportListWriteByCustomer(request);
    }
}
