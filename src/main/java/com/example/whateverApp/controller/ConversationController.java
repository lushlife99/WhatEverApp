package com.example.whateverApp.controller;

import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.model.document.Chat;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.service.ConversationImpl;
import com.example.whateverApp.service.WorkServiceImpl;
import com.example.whateverApp.service.interfaces.ConversationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jdk.jfr.MemoryAddress;
import lombok.RequiredArgsConstructor;
import org.bson.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@CrossOrigin
public class ConversationController {

    private final WorkServiceImpl workService;
    private final ConversationImpl conversationService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @PostMapping("/api/conversation/{participantId}")
    @NotNull
    public Conversation createChat(@RequestBody WorkDto workDto ,@PathVariable Long participantId, HttpServletRequest request){
        return conversationService.openAndMessage(request, participantId,workDto);
    }
//
//    @MessageMapping("/work/{conversationId}")
//    public void sendWork(@RequestBody WorkDto workDto, @DestinationVariable String conversationId) throws JsonProcessingException {
//        System.out.println("ConversationController.sendWork");
//        simpMessagingTemplate.convertAndSend("/topic/chat/" + conversationId , conversationService.sendWork(conversationId, workDto));
//    }

    @MessageMapping("/chat/{conversationId}")
    public void sendChat(@RequestBody Chat chat, @DestinationVariable String conversationId){
        simpMessagingTemplate.convertAndSend("/topic/chat/" + conversationId , conversationService.sendChatting(chat, conversationId));
    }

    @MessageMapping("/work/{conversationId}")
    public void sendWork(@RequestBody WorkDto workDto, @DestinationVariable String conversationId, @Header("Authorization") String jwtToken) throws JsonProcessingException{
        System.out.println(jwtToken);
        simpMessagingTemplate.convertAndSend("/topic/chat/" + conversationId , conversationService.sendWork(conversationId, workDto, jwtToken));
    }


    @MessageMapping("/card/{conversationId}")
    public void sendCard(@RequestBody Chat chat, @DestinationVariable String conversationId){
        simpMessagingTemplate.convertAndSend("/topic/chat/"+conversationId , conversationService.sendCard(chat, conversationId));
    }

    @GetMapping("/api/conversations")
    public List<Conversation> getConversations(HttpServletRequest request){
        return conversationService.getConversations(request);
    }

    @MessageMapping("/pub/conversation/{participantId}")
    public void getConversations(@RequestBody WorkDto workDto ,@PathVariable Long participantId, HttpServletRequest request){
        simpMessagingTemplate.convertAndSend("/queue/"+participantId, conversationService.openAndMessage(request, participantId,workDto));
    }


}