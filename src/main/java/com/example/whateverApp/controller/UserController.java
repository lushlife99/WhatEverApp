package com.example.whateverApp.controller;


import com.example.whateverApp.dto.TokenInfo;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {


    private final UserServiceImpl userService;
    private final UserServiceImpl userServiceImpl;

    // 회원가입 API
    @PostMapping("/join")
    public Boolean join(@Validated @RequestBody User user) {
        return userService.join(user);
    }

    // 로그인 API
    @PostMapping("/login")
    public TokenInfo login(@RequestBody User user) {
        System.out.println("UserController.login");
        return userService.login(user);
    }
}
