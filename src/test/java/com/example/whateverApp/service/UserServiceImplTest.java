package com.example.whateverApp.service;

import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceImplTest {


    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthenticationManager authenticationManagerBuilder;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;


    @Test
    public void loginTest() {
        System.out.println("m");
    }
}