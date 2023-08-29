package com.example.whateverApp.controller;

import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.example.whateverApp.service.FirebaseCloudMessageService;
import com.example.whateverApp.service.RewardService;
import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;
    private final FirebaseCloudMessageService firebaseCloudMessageService;
    private final RewardService rewardService;
    @PostMapping("/test/user")
    public String addUser(@RequestBody User user){
        userRepository.save(user);
        return "ok";
    }

    @GetMapping("/api/test")
    public String 권한체크(){
        return "ok";
    }

    @GetMapping("/fcm")
    public String fcmTest() throws IOException, FirebaseMessagingException {
        System.out.println("TestController.fcmTest");
        List<User> userDtos = new ArrayList<>();
        User user = userRepository.findByUserId("a1").get();
        User user1 = userRepository.findByUserId("a2").get();
        userDtos.add(user);
        userDtos.add(user1);

        return firebaseCloudMessageService.sendGroupTest(userDtos, "title", "body");
    }

    @GetMapping("/transfer")
    public UserDto transferTest(@RequestParam("amount") int amount, HttpServletRequest request) throws IOException {
        return rewardService.transfer(amount, request);
    }

    @PostMapping("/testA")
    public void testt(HttpServletRequest request){
        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()){
            System.out.println(headerNames.nextElement());

        }

        System.out.println("TestController.testt");
    }
}
