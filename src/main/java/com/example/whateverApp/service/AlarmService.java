package com.example.whateverApp.service;

import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.entity.Alarm;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.AlarmRepository;
import com.example.whateverApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;
    private final LocationServiceImpl locationService;
    private final FirebaseCloudMessageService fcmService;

//    public void sendNearByHelper(Location location, Work work) throws IOException {
//        String title = "근처에 새로운 심부름이 등록되었습니다.";
//        String body = work.getTitle();
//
//        List<User> aroundHelperList = locationService.getAroundHelperList(location);
//        for (User user : aroundHelperList)
//            if(!user.getNotification())
//                aroundHelperList.remove(user);
//
//        if(fcmService.sendMessageGroup(aroundHelperList, title, body)){
//            for (User user : aroundHelperList) {
//                Alarm alarm = Alarm.builder()
//                        .user(user)
//                        .title(title)
//                        .body(body)
//                        .build();
//                alarmRepository.save(alarm);
//            }
//        }
//    }
}
