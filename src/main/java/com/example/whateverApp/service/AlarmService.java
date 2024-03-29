package com.example.whateverApp.service;

import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.entity.Alarm;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.jpaRepository.AlarmRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public List<Alarm> getAlarms(HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request);
        List<Alarm> alarmList = user.getAlarmList();

        alarmRepository.saveAll(alarmList);
        return user.getAlarmList();
    }

    public void setSeenTrue(HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request);
        List<Alarm> list = alarmRepository.findAll().stream()
                .filter(
                        a -> user.getId().equals(a.getUser().getId())
                ).filter(
                        a -> a.getSeen().equals(Boolean.FALSE)
                ).toList();

        for (Alarm alarm : list) {
            alarm.setSeen(true);
        }

        alarmRepository.saveAll(list);
    }

    public int getSeenCount(HttpServletRequest request) {
        User user = jwtTokenProvider.getUser(request);
        List<Alarm> alarmList = user.getAlarmList();
        int totalCount = 0;
        for (Alarm alarm : alarmList) {
            if(alarm.getSeen().equals(Boolean.FALSE))
                totalCount++;
        }

        return totalCount;
    }

    public Alarm send(User user, String title, String body, String routeType){
        Alarm alarm = Alarm.builder().title(title).body(body).seen(Boolean.FALSE).user(user).routeType(routeType).build();
        return alarmRepository.save(alarm);
    }

    public List<Alarm> sendGroup(List<User> userList, String title, String body) {
        List<Alarm> list = new ArrayList<>();
        for (User user : userList) {
            Alarm alarm = Alarm.builder()
                    .user(user)
                    .title(title)
                    .body(body)
                    .seen(false)
                    .build();
            list.add(alarm);
        }
        return alarmRepository.saveAll(list);
    }
}
