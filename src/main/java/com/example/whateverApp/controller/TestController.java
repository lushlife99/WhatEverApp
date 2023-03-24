package com.example.whateverApp.controller;

import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;


    @PostMapping("/test/login")
    public User addUser(@RequestBody User user) {
        return userRepository.save(user);

    }

    @GetMapping("/test")
    public String test(){
        System.out.println("TestController.test");
        return "ok";
    }

    @GetMapping("/api/test")
    public String 권한체크(){

        return "ok";
    }
}

