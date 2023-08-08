package com.example.whateverApp.controller;

import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/reward/beforeWork")
    public void beforeWork(WorkDto workDto, HttpServletRequest request){
        paymentService.beforeWork(workDto, request);
    }

    @PostMapping("/reward/afterWork")
    public void AfterWork(WorkDto workDto, HttpServletRequest request) {
        paymentService.afterWork(workDto, request);
    }

    /**
     *
     * 음.. 이거 충전할 때도 검증이 필요함..
     * 특히 돈에 관련된거라 나중에 꼭 수정.
     * @param request
     */
    @PostMapping("/reward/charge")
    public void chargeReward(HttpServletRequest request){
        paymentService.chargeReward(request);
    }
}
