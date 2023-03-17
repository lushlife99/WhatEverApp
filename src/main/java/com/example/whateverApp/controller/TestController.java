package com.example.whateverApp.controller;

import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;


    @PostMapping("/test/user")
    public String addUser(@RequestBody User user){
        userRepository.save(user);
        return "ok";
    }
}
