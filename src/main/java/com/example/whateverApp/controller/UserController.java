package com.example.whateverApp.controller;


import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.service.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    @GetMapping("/userInfo")
    public UserDto getMyInfo(HttpServletRequest request) throws IOException {
        return userService.getMyInfo(request);
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

    @GetMapping("/user/{userId}")
    public UserDto getUserInfo(@PathVariable Long userId) throws IOException {
        return userService.getUserInfo(userId);
    }

    @PutMapping("/user")
    public UserDto modifyPassword(@RequestParam String password, HttpServletRequest request){
        return userService.modifyPassword(password, request);
    }
}
