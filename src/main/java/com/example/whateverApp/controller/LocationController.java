package com.example.whateverApp.controller;

import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.service.LocationServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/location")
public class LocationController {

    private final LocationServiceImpl locationService;

    @PutMapping("/findHelper/distance")
    public List<UserDto> findNearByHelper(@RequestBody Location location, HttpServletRequest request) throws IOException {
        return locationService.findHelperByDistance(location, request);
    }


    @PutMapping("/user")
    public UserDto setUserLocation(HttpServletRequest request, Location location){
        return locationService.setUserLocation(request, location);
    }

    /**
     * updateHelperLocaiton
     *
     * HelperLocation과 userLocation의 차이?
     * HelperLocation은 헬퍼의 위치를 추적하고 저장하는 서비스를 사용할 때 저장되는 위치의 이름이다.
     * 심부름 중인 헬퍼의 위치 정보를 다룰 때 쓰인다.
     * @param location
     * @param workId
     * @return
     */

    @PostMapping("/helperLocations/{workId}")
    public Boolean updateHelperLocation(@RequestBody Location location, @PathVariable Long workId){
        return locationService.setHelperLocation(location, workId);
    }

    @GetMapping("/helperLocations/{workId}")
    public List<Location> getHelperLocationList(@PathVariable Long workId){
        return locationService.getHelperLocationLists(workId);
    }
    @GetMapping("/helperLocation/{workId}")
    public void getHelperLocation(@PathVariable Long workId){
        locationService.getHelperLocation(workId);
    }

    @PostMapping("/sendToCustomer/{workId}")
    public void sendHelperLocationToCustomer(@PathVariable Long workId, @RequestBody Location location, HttpServletRequest request){
        locationService.sendHelperLocationToCustomer(workId, location, request);
    }
}