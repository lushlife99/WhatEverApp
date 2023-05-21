package com.example.whateverApp.service;

import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceImplTest {


    @MockBean
    UserRepository userRepository;

    AuthenticationManager authenticationManagerBuilder;
    JwtTokenProvider jwtTokenProvider;


    @Test
    void loginTest() {

    }
}