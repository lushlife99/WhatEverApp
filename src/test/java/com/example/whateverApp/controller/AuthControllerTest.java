package com.example.whateverApp.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.UserRepository;
import com.example.whateverApp.service.UserServiceImpl;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.Assert;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthControllerTest{

    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("회원가입 테스트")
    public void join(){
        User user = User.builder().name("t1").userId("t1231").password("1234").latitude(123.123).longitude(123.123).build();
        when(userService.join(user)).thenReturn(user);

    }

}