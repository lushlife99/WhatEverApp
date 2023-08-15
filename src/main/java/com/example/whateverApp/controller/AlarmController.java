package com.example.whateverApp.controller;

import com.example.whateverApp.model.entity.Alarm;
import com.example.whateverApp.service.AlarmService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alarm")
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping
    public List<Alarm> getAlarmList(HttpServletRequest request){
        return alarmService.getAlarms(request);
    }

    @PutMapping
    public void setSeenTrue(HttpServletRequest request){
        alarmService.setSeenTrue(request);
    }
}
