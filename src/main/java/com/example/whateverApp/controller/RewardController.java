package com.example.whateverApp.controller;

import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.service.RewardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RewardController {

    private final RewardService rewardService;

    @GetMapping("/reward/transfer")
    public UserDto rewardTransfer(@RequestParam("amount") int amount, HttpServletRequest request) throws IOException {
        return rewardService.transfer(amount, request);
    }
}
