package com.example.whateverApp.service;

import com.example.whateverApp.dto.MessageDto;
import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.Enum.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.document.HelperLocation;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.HelperLocationRepository;
import com.example.whateverApp.repository.UserRepository;
import com.example.whateverApp.repository.WorkRepository;
import com.example.whateverApp.service.interfaces.LocationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
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

    @Value("${file:dir}")
    private String fileDir;

    @Override
    public Page<UserDto> findHelperByDistance(Pageable pageable, Location location, HttpServletRequest request) throws MalformedURLException, IOException {
        User user = jwtTokenProvider.getUser(request)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        //현재 위도 좌표 (y 좌표)
        double nowLatitude = location.getLatitude();
        //현재 경도 좌표 (x 좌표)
        double nowLongitude = location.getLongitude();

        double EARTH_RADIUS = 6371;

        //m당 y 좌표 이동 값
        double mForLatitude = (1 / (EARTH_RADIUS * 1 * (Math.PI / 180))) / 1000;
        //m당 x 좌표 이동 값
        double mForLongitude = (1 / (EARTH_RADIUS * 1 * (Math.PI / 180) * Math.cos(Math.toRadians(nowLatitude)))) / 1000;

        //현재 위치 기준 검색 거리 좌표
        double maxY = nowLatitude + (5000 * mForLatitude);
        double minY = nowLatitude - (5000 * mForLatitude);
        double maxX = nowLongitude + (5000 * mForLongitude);
        double minX = nowLongitude - (5000 * mForLongitude);

        //해당되는 좌표의 범위 안에 있는 유저 찾기. filter

        List<User> tempAroundHelperList = userRepository.findAll().stream().filter(u -> {
            return u.getLatitude().compareTo(maxY) <= 0;
        }).filter(u -> {
            return u.getLatitude().compareTo(minY) >= 0;
        }).filter(u -> {
            return u.getLongitude().compareTo(maxX) <= 0;
        }).filter(u -> {
            return u.getLongitude().compareTo(minX) >= 0;
        }).toList();

        List<UserDto> resultAroundUserList = new ArrayList<>();
        UserDto userDto;
        //정확한 거리계산, And 유저 거리저장.
        for (User user1 : tempAroundHelperList) {
            double distance = getDistance(nowLatitude, nowLongitude, user1.getLatitude(), user1.getLongitude());
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
        Collections.sort(resultAroundUserList, (u1, u2) -> {
                return u1.getDistance().compareTo(u2.getDistance());
        });
        Page<UserDto> page = new PageImpl<>(resultAroundUserList, pageable, resultAroundUserList.size());
        return page;
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
        if (work.isProceeding())
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