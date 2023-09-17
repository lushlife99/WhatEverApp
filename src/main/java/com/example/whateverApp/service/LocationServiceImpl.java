package com.example.whateverApp.service;

import com.example.whateverApp.dto.MessageDto;
import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.WorkProceedingStatus;
import com.example.whateverApp.model.document.HelperLocation;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.mongoRepository.HelperLocationRepository;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.example.whateverApp.repository.jpaRepository.WorkRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl{

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final WorkRepository workRepository;
    private final HelperLocationRepository helperLocationRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserServiceImpl userService;
    static final double EARTH_RADIUS = 6371;
    @Value("${file:dir}")
    private String fileDir;
    public List<UserDto> findHelperByDistance(Location location, HttpServletRequest request) throws IOException {
        User user = jwtTokenProvider.getUser(request);
        List<User> tempAroundHelperList = getAroundHelperList(location);
        if(tempAroundHelperList.contains(user)){
            System.out.println("LocationServiceImpl.findHelperByDistance");
        }
        List<UserDto> resultAroundUserList = new ArrayList<>();
        UserDto userDto;

        for (User aroundHelper : tempAroundHelperList) {
            double distance = getDistance(location.getLatitude(), location.getLongitude(), aroundHelper.getLatitude(), aroundHelper.getLongitude());
            if (distance < 5000) {
                userDto = new UserDto(aroundHelper);
                userDto.setDistance(distance);
                userDto.setImage(userService.getUserImage(aroundHelper));
                if (user.getId() != userDto.getId())
                    resultAroundUserList.add(userDto);
            }
        }
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
    public UserDto setUserLocation(HttpServletRequest request, Location location) {
        User user = jwtTokenProvider.getUser(request);

        user.setLatitude(location.getLatitude());
        user.setLongitude(location.getLongitude());
        return new UserDto(userRepository.save(user));
    }
    public Boolean setHelperLocation(Location location, Long workId) {
        Work work = workRepository.findById(workId).get();
        if (work.getProceedingStatus().equals(WorkProceedingStatus.FINISHED))
            return false;

        HelperLocation helperLocation = helperLocationRepository.findByWorkId(work.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.HELPER_LOCATION_NOT_FOUND));

        List<Location> locationList = helperLocation.getLocationList();
        locationList.add(location);
        helperLocation.setLocationList(locationList);
        helperLocationRepository.save(helperLocation);
        if(locationList.size() > 60)
            return false;

        return true;
    }
    public List<Location> getHelperLocationLists(Long workId){
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));

        if(!work.getProceedingStatus().equals(WorkProceedingStatus.STARTED) && work.getDeadLineTime() != 1)
            throw new CustomException(ErrorCode.ALREADY_PROCEED_WORK);

        HelperLocation helperLocation = helperLocationRepository.findByWorkId(workId)
                .orElseThrow(() -> new CustomException(ErrorCode.HELPER_LOCATION_NOT_FOUND));

        return helperLocation.getLocationList();
    }
    public void getHelperLocation(Long workId){
        Work work = workRepository.findById(workId).orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
        simpMessagingTemplate.convertAndSend("/queue/"+work.getHelper().getId(), new MessageDto("SendLocation", new WorkDto(work)));
    }
    public void sendHelperLocationToCustomer(Long workId, Location location, HttpServletRequest request){

        User user = jwtTokenProvider.getUser(request);
        Work work = workRepository.findById(workId).orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
        if(!user.getUserId().equals(work.getHelper().getUserId()))
            throw new CustomException(ErrorCode.BAD_REQUEST);

        simpMessagingTemplate.convertAndSend("/queue/"+work.getCustomer().getId(), new MessageDto("HelperLocation", location));
    }
}