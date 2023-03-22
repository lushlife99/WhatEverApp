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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/location")
public class LocationController {

    private final LocationServiceImpl locationService;

    @GetMapping("/findHelper/distance")
    public Page<UserResponseDto> findHelperByDistance(@PageableDefault(size = 10)Pageable pageable, HttpServletRequest request){
        return locationService.findHelperByDistance(pageable, request);
    }

    @GetMapping("/findHelper/rating")
    public Page<UserResponseDto> findHelperByRating(@PageableDefault(size = 10, sort="rating", direction = Sort.Direction.DESC)Pageable pageable, HttpServletRequest request){
        return locationService.findHelper(pageable, request);
    }

    @GetMapping("/findHelper/avgReactTime")
    public Page<UserResponseDto> findHelperByReactTime(@PageableDefault(size = 10, sort="avgReactTime")Pageable pageable, HttpServletRequest request){
        return locationService.findHelper(pageable, request);
    }

    @PutMapping("/user")
    public Location setUserLocation(HttpServletRequest request){
        return locationService.setUserLocation(request);
    }

}