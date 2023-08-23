package com.example.whateverApp.controller;


import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.service.FirebaseCloudMessageService;
import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.validation.Valid;
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

    @PostMapping("/api/fcm/sendNearbyHelper")
    public void sendNearbyHelper(@RequestBody WorkDto workDto) throws FirebaseMessagingException, IOException {
        System.out.println("FcmController.sendNearbyHelper");
        firebaseCloudMessageService.sendNearByHelper(workDto);
    }

}
