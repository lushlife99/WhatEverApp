package com.example.whateverApp.service;

import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;

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