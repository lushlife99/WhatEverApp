package com.example.whateverApp.controller;


import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.service.FirebaseCloudMessageService;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fcm")
public class FcmController {

    private final FirebaseCloudMessageService firebaseCloudMessageService;

    @PostMapping("/chatNotification/{conversationId}")
    public ResponseEntity notifyOnChat(@PathVariable String conversationId) throws IOException{
        firebaseCloudMessageService.chatNotification(conversationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sendNearbyHelper")
    public void notifyCreatedWork (@RequestBody WorkDto workDto) throws FirebaseMessagingException{
        firebaseCloudMessageService.sendNearByHelper(workDto);
    }

}
