package com.example.whateverApp.config;

import com.example.whateverApp.service.AdminService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CommonConfig {


    private final AdminService adminService;

    private final String adminUsername = "admin";

    private final String adminPassword = "1234";

    @PostConstruct
    public void initAdminUser() {
        adminService.joinAdmin(adminUsername, adminPassword);
    }
}
