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
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/location")
public class LocationController {

    private final LocationServiceImpl locationService;

    @PutMapping("/findHelper/distance")
    public Page<UserDto> findHelperByDistance(@PageableDefault(size = 10)Pageable pageable,
                                              @RequestBody Location location, HttpServletRequest request, HttpServletResponse response) throws MalformedURLException, IOException {
        response.setContentType("multipart/form-data");
        return locationService.findHelperByDistance(pageable, location, request);
    }

    @PutMapping("/user")
    public UserDto setUserLocation(HttpServletRequest request, Location location){
        return locationService.setUserLocation(request, location);
    }

    @PostMapping("/helperLocation/{workId}")
    public Boolean updateHelperLocation(@RequestBody Location location, @PathVariable Long workId){
        return locationService.setHelperLocation(location, workId);
    }

    @GetMapping("/helperLocation/{workId}")
    public List<Location> getHelperLocationList(@PathVariable Long workId){
        return locationService.getHelperLocationList(workId);
    }

//    @PostMapping("/helperLocation/recordStart")
//    public void locationRecordStart(@RequestBody WorkDto workDto) throws InterruptedException{
//        locationService.recordLocation(workDto);
//    }

}