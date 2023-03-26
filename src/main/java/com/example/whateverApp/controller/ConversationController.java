package com.example.whateverApp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ConversationController {

    @MessageMapping("/pub/hello")
    @SendTo("/sub/greeting")
    public void greeting(@RequestBody Object ob) throws Exception {
        System.out.println("ConversationController.greeting");
    }
}
