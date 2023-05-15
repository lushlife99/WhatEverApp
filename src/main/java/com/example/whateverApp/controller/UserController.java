package com.example.whateverApp.controller;


import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.service.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;


@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class UserController {

    private final UserServiceImpl userService;

    @PutMapping("/userInfo")
    public UserDto updateUserInfo(@RequestBody UserDto userDto, HttpServletRequest request){
        return userService.update(userDto, request);
    }

    @PutMapping("/userInfo/image")
    public void imageUpdate(@RequestParam MultipartFile image, HttpServletRequest request) throws IOException {
        userService.updateProfileImage(request, image);
    }

//    @GetMapping("/userInfo/image")
//    public Resource getImage(HttpServletRequest request) throws MalformedURLException {
//        return userService.getUserImage(request);
//    }

    @GetMapping("/userInfo")
    public UserDto getUserInfo(HttpServletRequest request) throws MalformedURLException, IOException {
        return userService.getUserInfo(request);
    }

    @PutMapping("/userLocation")
    public Location setUserLocation(@RequestBody Location location,  HttpServletRequest request){
        return userService.setUserLocation(location, request);
    }

    @PutMapping("/fcm/{token}")
    public ResponseEntity updateNotificationToken(@PathVariable String token, HttpServletRequest request){
        userService.updateNotificationToken(token, request);
        return ResponseEntity.ok().build();
    }

}
