package com.example.whateverApp.controller;


import com.example.whateverApp.dto.TokenInfo;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.service.UserServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public TokenInfo login(@RequestBody User user, HttpServletResponse response) {
        return userService.login(user, response);
    }

    //Token Update
    @PutMapping("/token")
    public TokenInfo IssueToken(HttpServletRequest request, HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        return userService.issueToken(request, response);
    }

}
