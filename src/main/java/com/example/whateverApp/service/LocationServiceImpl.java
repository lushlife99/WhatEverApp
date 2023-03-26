package com.example.whateverApp.service;

import com.example.whateverApp.dto.UserResponseDto;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.UserRepository;
import com.example.whateverApp.service.interfaces.LocationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;




    public Page<UserResponseDto> findHelperByDistance(Pageable pageable, Location location, HttpServletRequest request) {
        User user = userRepository.findByUserId(jwtTokenProvider.getAuthentication(request.getHeader("Authorization").substring(7)).getName()).get();

        //현재 위도 좌표 (y 좌표)
        double nowLatitude = location.getLatitude();
        //현재 경도 좌표 (x 좌표)
        double nowLongitude = location.getLongitude();

        double EARTH_RADIUS = 6371;

        //m당 y 좌표 이동 값
        double mForLatitude =(1 /(EARTH_RADIUS* 1 *(Math.PI/ 180)))/ 1000;
        //m당 x 좌표 이동 값
        double mForLongitude =(1 /(EARTH_RADIUS* 1 *(Math.PI/ 180)* Math.cos(Math.toRadians(nowLatitude))))/ 1000;

        //현재 위치 기준 검색 거리 좌표
        double maxY = nowLatitude +(5000* mForLatitude);
        double minY = nowLatitude -(5000* mForLatitude);
        double maxX = nowLongitude +(5000* mForLongitude);
        double minX = nowLongitude -(5000* mForLongitude);

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
        if(tempAroundHelperList.contains(user)) {
            tempAroundHelperList.remove(user);
        }

        List<UserResponseDto>resultAroundUserList = new ArrayList<>();
        UserResponseDto userDto;
        //정확한 거리계산, And 유저 거리저장.
        for(User user1 : tempAroundHelperList){
            double distance = getDistance(nowLatitude, nowLongitude, user1.getLatitude(), user1.getLongitude());
            if(distance < 5000) {
                userDto = new UserResponseDto(user1);
                userDto.setDistance(distance);
                resultAroundUserList.add(userDto);
            }
        }
        resultAroundUserList.remove(0);
        Collections.sort(resultAroundUserList, (u1, u2)->{
            return u1.getDistance().compareTo(u2.getDistance());
        });
        Page<UserResponseDto> page = new PageImpl<>(resultAroundUserList, pageable, resultAroundUserList.size());

        return page;
    }
    @Override
    public Page<UserResponseDto> findHelper(Pageable pageable, Location location, HttpServletRequest request) {
        User user = userRepository.findByUserId(jwtTokenProvider.getAuthentication(request.getHeader("Authorization").substring(7)).getName()).get();
        //현재 위도 좌표 (y 좌표)
        double nowLatitude = location.getLatitude();
        //현재 경도 좌표 (x 좌표)
        double nowLongitude = location.getLongitude();

        double EARTH_RADIUS = 6371;

        //m당 y 좌표 이동 값
        double mForLatitude =(1 /(EARTH_RADIUS* 1 *(Math.PI/ 180)))/ 1000;
        //m당 x 좌표 이동 값
        double mForLongitude =(1 /(EARTH_RADIUS* 1 *(Math.PI/ 180)* Math.cos(Math.toRadians(nowLatitude))))/ 1000;

        //현재 위치 기준 검색 거리 좌표
        double maxY = nowLatitude +(5000* mForLatitude);
        double minY = nowLatitude -(5000* mForLatitude);
        double maxX = nowLongitude +(5000* mForLongitude);
        double minX = nowLongitude -(5000* mForLongitude);

        //해당되는 좌표의 범위 안에 있는 유저 찾기. filter
        List<User> list = userRepository.findAll(pageable).stream().filter(u->{
            return u.getLatitude().compareTo(maxY) <= 0;
        }).filter(u->{
            return u.getLatitude().compareTo(minY) >= 0;
        }).filter(u->{
            return u.getLongitude().compareTo(maxX) <= 0;
        }).filter(u->{
            return u.getLongitude().compareTo(minX) >= 0;
        }).toList();
        User requestUser = userRepository.findByUserId(jwtTokenProvider.getAuthentication(request.getHeader("Authorization").substring(7)).getName()).get();

        ArrayList<User> tempAroundHelperList = new ArrayList<>(list);
        if(tempAroundHelperList.contains(user)) {
            tempAroundHelperList.remove(user);
        }

        List<UserResponseDto>resultAroundUserList = new ArrayList<>();
        UserResponseDto userDto;
        //정확한 거리계산, And 유저 거리저장.
        for(User user1 : tempAroundHelperList){
            double distance = getDistance(nowLatitude, nowLongitude, user1.getLatitude(), user1.getLongitude());
            if(distance < 5000) {
                userDto = new UserResponseDto(user1);
                userDto.setDistance(distance);
                resultAroundUserList.add(userDto);
            }
        }
        Page<UserResponseDto> page = new PageImpl<>(resultAroundUserList, pageable, resultAroundUserList.size());

        return page;
    }



    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        final double EARTH_RADIUS = 6371;
        double a = Math.sin(dLat/2)* Math.sin(dLat/2)+ Math.cos(Math.toRadians(lat1))* Math.cos(Math.toRadians(lat2))* Math.sin(dLon/2)* Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d =EARTH_RADIUS* c * 1000;    // Distance in m
        return d;
    }

    @Override
    public Location setUserLocation(HttpServletRequest request) {
        return null;
    }

    @Override
    public Location setSellerLocation(HttpServletRequest request, Work work) {
        return null;
    }

}