package com.example.whateverApp.controller;


import com.example.whateverApp.dto.TokenInfo;
import com.example.whateverApp.dto.UserResponseDto;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.service.UserServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
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
    public UserResponseDto updateUserInfo(@RequestBody User user, HttpServletRequest request){
        return userService.update(user, request);
    }

    @PutMapping("/userInfo/image")
    public void imageUpdate(@RequestParam MultipartFile image, HttpServletRequest request) throws IOException {
        User user = userService.updateProfileImage(request, image);
    }

    @GetMapping("/userInfo/image")
    public Resource getImage(HttpServletRequest request) throws MalformedURLException {
        return userService.getUserImage(request);
    }
    @GetMapping("/userInfo")
    public UserResponseDto getUserInfo(HttpServletRequest request){
        return userService.getUserInfo(request);
    }

}
