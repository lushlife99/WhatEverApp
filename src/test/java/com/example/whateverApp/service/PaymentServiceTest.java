package com.example.whateverApp.service;

import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.repository.UserRepository;
import com.example.whateverApp.repository.WorkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private WorkRepository workRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;



    @Test
    public void test(){
        System.out.println();
    }
}