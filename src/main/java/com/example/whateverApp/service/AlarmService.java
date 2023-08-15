package com.example.whateverApp.service;

import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.entity.Alarm;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.jpaRepository.AlarmRepository;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;
    private final LocationServiceImpl locationService;
    private final FirebaseCloudMessageService fcmService;
    private final JwtTokenProvider jwtTokenProvider;

    public void sendNearByHelper(Location location, Work work) throws IOException {
        String title = "근처에 새로운 심부름이 등록되었습니다.";
        String body = work.getTitle();

        List<User> aroundHelperList = locationService.getAroundHelperList(location);
        for (User user : aroundHelperList)
            if(!user.getNotification())
                aroundHelperList.remove(user);

        if(aroundHelperList.contains(work.getCustomer()))
            aroundHelperList.remove(work.getCustomer());


        if(fcmService.sendMessageGroup(aroundHelperList, title, body)){
            List<Alarm> list = new ArrayList<>();
            for (User user : aroundHelperList) {
                Alarm alarm = Alarm.builder()
                        .user(user)
                        .title(title)
                        .body(body)
                        .build();

                list.add(alarm);
            }
            alarmRepository.saveAll(list);
        }
    }

    public List<Alarm> getAlarms(HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return user.getAlarmList();
    }

    public void setSeenTrue(HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
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
}
