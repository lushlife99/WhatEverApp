package com.example.whateverApp.controller;


import com.example.whateverApp.dto.ReportDto;
import com.example.whateverApp.dto.TokenInfo;
import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.el.parser.Token;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/admin/check")
    public Boolean isAdmin(HttpServletRequest request){
        return true;
    }

    @PostMapping("/loginAdmin")
    public TokenInfo adminLogin(@RequestBody User user, HttpServletResponse response){
        return adminService.login(user, response);
    }

    @GetMapping("/admin/reportList")
    public List<ReportDto> getReportList(){
        return adminService.getReportList();
    }

}
