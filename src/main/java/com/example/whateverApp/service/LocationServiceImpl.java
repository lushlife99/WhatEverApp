package com.example.whateverApp.service;

import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.document.HelperLocation;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.mongoRepository.HelperLocationRepository;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.example.whateverApp.repository.jpaRepository.WorkRepository;
import com.example.whateverApp.service.interfaces.LocationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserServiceImpl userService;
    private final WorkRepository workRepository;
    private final HelperLocationRepository helperLocationRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    static final double EARTH_RADIUS = 6371;
    @Value("${file:dir}")
    private String fileDir;

    @Override
    public List<UserDto> findHelperByDistance(Location location, HttpServletRequest request) throws IOException {
        User user = jwtTokenProvider.getUser(request)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<User> tempAroundHelperList = getAroundHelperList(location);


        List<UserDto> resultAroundUserList = new ArrayList<>();
        UserDto userDto;

        for (User user1 : tempAroundHelperList) {
            double distance = getDistance(location.getLatitude(), location.getLongitude(), user1.getLatitude(), user1.getLongitude());
            byte[] photoEncode;
            if (distance < 5000) {
                userDto = new UserDto(user1);
                userDto.setDistance(distance);
                Base64.Encoder encoder = Base64.getEncoder();
                File file = new File(fileDir + user1.getImageFileName());
                if (file.exists()) {
                    photoEncode = encoder.encode(new UrlResource("file:" + fileDir + user1.getImageFileName()).getContentAsByteArray());
                    userDto.setImage(new String(photoEncode, "UTF8"));
                }
                if (user.getId() != userDto.getId()) {
                    resultAroundUserList.add(userDto);
                }
            }
        }
        Collections.sort(resultAroundUserList, Comparator.comparing(UserDto::getDistance));
        return resultAroundUserList;
    }

    public List<User> getAroundHelperList(Location location){
        double nowLatitude = location.getLatitude();
        double nowLongitude = location.getLongitude();

        double mForLatitude = (1 / (EARTH_RADIUS * 1 * (Math.PI / 180))) / 1000;
        double mForLongitude = (1 / (EARTH_RADIUS * 1 * (Math.PI / 180) * Math.cos(Math.toRadians(nowLatitude)))) / 1000;

        double maxY = nowLatitude + (5000 * mForLatitude);
        double minY = nowLatitude - (5000 * mForLatitude);
        double maxX = nowLongitude + (5000 * mForLongitude);
        double minX = nowLongitude - (5000 * mForLongitude);


        return userRepository.findAll().stream()
                .filter(u -> u.getLatitude().compareTo(maxY) <= 0)
                .filter(u -> u.getLatitude().compareTo(minY) >= 0)
                .filter(u -> u.getLongitude().compareTo(maxX) <= 0)
                .filter(u -> u.getLongitude().compareTo(minX) >= 0).toList();
    }


    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        final double EARTH_RADIUS = 6371;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = EARTH_RADIUS * c * 1000;    // Distance in m
        return d;
    }

    @Override
    public UserDto setUserLocation(HttpServletRequest request, Location location) {
        User user = jwtTokenProvider.getUser(request)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        user.setLatitude(location.getLatitude());
        user.setLongitude(location.getLongitude());
        return new UserDto(userRepository.save(user));
    }

    @Override
    public Boolean setHelperLocation(Location location, Long workId) {
        Work work = workRepository.findById(workId).get();
        if (work.isFinished())
            return false;

        HelperLocation helperLocation = helperLocationRepository.findByWorkId(work.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.HELPERLOCATION_NOT_FOUND));

        List<Location> locationList = helperLocation.getLocationList();
        locationList.add(location);
        helperLocation.setLocationList(locationList);
        helperLocationRepository.save(helperLocation);
        return true;
    }

    public List<Location> getHelperLocationList(Long workId){
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));

        if(!work.isProceeding() && work.getDeadLineTime() != 1)
            throw new CustomException(ErrorCode.ALREADY_PROCEED_WORK);

        HelperLocation helperLocation = helperLocationRepository.findByWorkId(workId)
                .orElseThrow(() -> new CustomException(ErrorCode.HELPERLOCATION_NOT_FOUND));

        return helperLocation.getLocationList();
    }


}