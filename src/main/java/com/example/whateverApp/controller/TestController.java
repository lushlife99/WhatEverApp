package com.example.whateverApp.controller;

import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.example.whateverApp.service.FirebaseCloudMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;
    private final FirebaseCloudMessageService firebaseCloudMessageService;
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
    public void fcmTest() throws IOException {
        List<User> list = new ArrayList<>();
        User user = userRepository.findByUserId("chan").get();
        list.add(user);

        firebaseCloudMessageService.test(list, "aaa", "bbb");
    }

}
