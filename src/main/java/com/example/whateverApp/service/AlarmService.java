package com.example.whateverApp.service;

import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.AlarmRepository;
import com.example.whateverApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.whateverApp.service.LocationServiceImpl.EARTH_RADIUS;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;
    private final LocationServiceImpl locationService;
    private final FirebaseCloudMessageService fcmService;

    public void sendNearByHelper(Location location) {
        double nowLatitude = location.getLatitude();
        double nowLongitude = location.getLongitude();

        List<User> aroundHelperList = locationService.getAroundHelperList(location);

        for (User user : aroundHelperList) {
            if(user.getNotification()){
                String notificationToken = user.getNotificationToken();

            }


        }

    }
}
