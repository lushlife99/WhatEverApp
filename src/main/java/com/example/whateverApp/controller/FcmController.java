package com.example.whateverApp.controller;


import com.example.whateverApp.dto.FCMRequestDto;
import com.example.whateverApp.service.FirebaseCloudMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class FcmController {

    private final FirebaseCloudMessageService firebaseCloudMessageService;

    @PostMapping("/api/fcm")
    public ResponseEntity pushMessage(@RequestBody FCMRequestDto requestDTO) throws IOException {
        firebaseCloudMessageService.sendMessageTo(
                requestDTO.getTargetToken(),
                requestDTO.getTitle(),
                requestDTO.getBody());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/fcm/{conversationId}")
    public ResponseEntity notifyChat(@PathVariable String conversationId) throws IOException{
        firebaseCloudMessageService.chatNotification(conversationId);
        return ResponseEntity.ok().build();
    }



}
