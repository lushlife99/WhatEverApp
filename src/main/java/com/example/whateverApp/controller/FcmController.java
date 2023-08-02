package com.example.whateverApp.controller;


import com.example.whateverApp.service.FirebaseCloudMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class FcmController {

    private final FirebaseCloudMessageService firebaseCloudMessageService;

    @PostMapping("/api/fcm/{conversationId}")
    public ResponseEntity notifyChat(@PathVariable String conversationId) throws IOException{
        firebaseCloudMessageService.chatNotification(conversationId);
        return ResponseEntity.ok().build();
    }

}
