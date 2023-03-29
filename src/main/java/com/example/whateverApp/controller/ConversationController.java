package com.example.whateverApp.controller;

import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.service.WorkServiceImpl;
import com.example.whateverApp.service.interfaces.ConversationService;
import jakarta.servlet.http.HttpServletRequest;
import jdk.jfr.MemoryAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ConversationController {

    private final WorkServiceImpl workService;
    private final ConversationService conversationService;
    @MessageMapping("/hello")
    @SendTo("/sub/greeting")
    public String greeting() {
        System.out.println("ConversationController.greeting");
        return "Hi!!!!!!!!!!!";
    }

    @PostMapping("/conversation")
    public Conversation createChat(HttpServletRequest request, Long participantId ){
        return conversationService.open(request, participantId);
    }

    @MessageMapping("/work")
    @SendTo("/sub/chat/{id}")
    public Conversation sendWork(@RequestBody Work work, String conversationId,HttpServletRequest request){
        return conversationService.sendWork(request, conversationId, work);
    }
}
