package com.example.whateverApp.controller;

import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.service.RewardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RewardController {

    private final RewardService rewardService;

    @PostMapping("/reward/beforeWork")
    public void beforeWork(WorkDto workDto, HttpServletRequest request){
        rewardService.beforeWork(workDto, request);
    }

    @PostMapping("/reward/afterWork")
    public void AfterWork(WorkDto workDto, HttpServletRequest request) {
        rewardService.afterWork(workDto, request);
    }

    /**
     *
     * 음.. 이거 충전할 때도 검증이 필요함..
     * 특히 돈에 관련된거라 나중에 꼭 수정.
     * @param request
     */
    @PostMapping("/reward/charge")
    public void chargeReward(HttpServletRequest request){
        rewardService.chargeReward(request);
    }
}
