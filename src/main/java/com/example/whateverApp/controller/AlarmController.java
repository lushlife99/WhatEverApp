package com.example.whateverApp.controller;

import com.example.whateverApp.model.entity.Alarm;
import com.example.whateverApp.service.AlarmService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alarm")
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping
    public List<Alarm> getAlarmList(HttpServletRequest request) throws CloneNotSupportedException {
        List<Alarm> alarms = alarmService.getAlarms(request);
        List<Alarm> copyAlarmList = new ArrayList<>();
        for (Alarm alarm : alarms)
            copyAlarmList.add(alarm.clone());
        Collections.reverse(copyAlarmList);

        alarmService.setSeenTrue(request);
        return copyAlarmList;
    }

    @GetMapping("/seenCount")
    public int getSeenCount(HttpServletRequest request){
        return alarmService.getSeenCount(request);
    }

}
