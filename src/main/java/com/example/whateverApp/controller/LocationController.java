package com.example.whateverApp.controller;

import com.example.whateverApp.dto.UserResponseDto;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.service.LocationServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/location")
public class LocationController {

    private final LocationServiceImpl locationService;

    @PutMapping("/findHelper/distance")
    public Page<UserResponseDto> findHelperByDistance(@PageableDefault(size = 10)Pageable pageable,
                                                      @RequestBody Location location, HttpServletRequest request) throws MalformedURLException {
        return locationService.findHelperByDistance(pageable, location, request);
    }

    @PutMapping("/findHelper/rating")
    public Page<UserResponseDto> findHelperByRating(@PageableDefault(size = 10, sort="rating", direction = Sort.Direction.DESC)Pageable pageable,
                                                    @RequestBody Location location, HttpServletRequest request) throws MalformedURLException{
        return locationService.findHelper(pageable, location, request);
    }

    @PutMapping("/findHelper/avgReactTime")
    public Page<UserResponseDto> findHelperByReactTime(@PageableDefault(size = 10, sort="avgReactTime")Pageable pageable,
                                                       @RequestBody Location location, HttpServletRequest request) throws MalformedURLException{
        return locationService.findHelper(pageable, location, request);
    }

    @PutMapping("/user")
    public UserResponseDto setUserLocation(HttpServletRequest request, Location location){
        return locationService.setUserLocation(request, location);
    }

}