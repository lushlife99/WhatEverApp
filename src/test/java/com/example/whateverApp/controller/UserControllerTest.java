package com.example.whateverApp.controller;

import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.repository.UserRepository;
import com.example.whateverApp.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServiceImpl userService;
    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MockHttpServletRequest httpServletRequest;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    /**
     * request에 jwt를 넣어주는 과정.
     */
    @BeforeEach
    void beforeEach(){

    }

    @Test
    void updateUserInfo() {

    }

    @Test
    void imageUpdate() {

    }

    @Test
    void getUserInfo() {
        //userService.getUserInfo()

    }

    @Test
    void setUserLocation() {

    }

    @Test
    void updateNotificationToken() {

    }
}