package com.example.whateverApp.controller;

import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;
    @PostMapping("/test/user")
    public String addUser(@RequestBody User user){
        userRepository.save(user);
        return "ok";
    }

    @GetMapping("/api/test")
    public String 권한체크(){
        return "ok";
    }


}
