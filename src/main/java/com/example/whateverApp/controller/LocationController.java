package com.example.whateverApp.controller;

import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.service.LocationServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/location")
public class LocationController {

    private final LocationServiceImpl locationService;

    @PutMapping("/findHelper/distance")
    public Page<UserDto> findHelperByDistance(@PageableDefault(size = 10)Pageable pageable,
                                              @RequestBody Location location, HttpServletRequest request, HttpServletResponse response) throws MalformedURLException, IOException {
        /**
         * 나중에 이거 봐보기.
         *
         * 일단 되는지 보자. 안되면 아래 방법 써보기.
         *
         * 클라이언트에서 dataType을 multipart/form-data로 정의해놓고
         * 받아지는지 확인하기.
         */
        response.setContentType("multipart/form-data");
        return locationService.findHelperByDistance(pageable, location, request);
    }

    @PutMapping("/findHelper/rating")
    public Page<UserDto> findHelperByRating(@PageableDefault(size = 10, sort="rating", direction = Sort.Direction.DESC)Pageable pageable,
                                            @RequestBody Location location, HttpServletRequest request) throws MalformedURLException{
        System.out.println("LocationController.findHelperByRating");
        return locationService.findHelper(pageable, location, request);
    }

    @PutMapping("/findHelper/avgReactTime")
    public Page<UserDto> findHelperByReactTime(@PageableDefault(size = 10, sort="avgReactTime")Pageable pageable,
                                               @RequestBody Location location, HttpServletRequest request) throws MalformedURLException{
        return locationService.findHelper(pageable, location, request);
    }

    @PutMapping("/user")
    public UserDto setUserLocation(HttpServletRequest request, Location location){
        return locationService.setUserLocation(request, location);
    }

    @PostMapping("/helperLocation/{workId}")
    public Location updateHelperLocation(@RequestBody Location location, @PathVariable Long workId){
        return locationService.setHelperLocation(location, workId);
    }

    @PostMapping("/helperLocation/recordStart")
    public void locationRecordStart(@RequestBody WorkDto workDto) throws InterruptedException{
        locationService.recordLocation(workDto);
    }

}