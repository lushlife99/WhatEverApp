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
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/location")
public class LocationController {

    private final LocationServiceImpl locationService;

    @PutMapping("/findHelper/distance")
    public List<UserDto> findHelperByDistance(@RequestBody Location location, HttpServletRequest request) throws IOException {
        return locationService.findHelperByDistance(location, request);
    }

    /**
     * setUserLocation -> 현재 자신의 위치를 설정하는 컨트롤러.
     * 여기서 설정된 위치를 기반으로 자신의 helper활동을 할 수 있음.
     *
     * @param request
     * @param location
     * @return
     */

    @PutMapping("/user")
    public UserDto setUserLocation(HttpServletRequest request, Location location){
        return locationService.setUserLocation(request, location);
    }

    /**
     * updateHelperLocaiton
     *
     * HelperLocation과 userLocation의 차이?
     * HelperLocation은 헬퍼의 위치를 추적하고 저장하는 서비스를 사용할 때 저장되는 위치의 이름이다.
     *
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