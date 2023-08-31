package com.example.whateverApp.controller;

import com.example.whateverApp.dto.ConversationDto;
import com.example.whateverApp.dto.MessageDto;
import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.document.Chat;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.service.ConversationImpl;
import com.example.whateverApp.service.WorkServiceImpl;
import com.example.whateverApp.service.interfaces.ConversationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.CreateIndexCommitQuorum;
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

    private final ConversationImpl conversationService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @PostMapping("/api/conversation/{participantId}")
    @NotNull
    public ConversationDto createChat(@RequestBody WorkDto workDto , @PathVariable Long participantId, HttpServletRequest request){
        return new ConversationDto(conversationService.openAndMessage(request, participantId,workDto));
    }
//
//    @MessageMapping("/work/{conversationId}")
//    public void sendWork(@RequestBody WorkDto workDto, @DestinationVariable String conversationId) throws JsonProcessingException {
//        System.out.println("ConversationController.sendWork");
//        simpMessagingTemplate.convertAndSend("/topic/chat/" + conversationId , conversationService.sendWork(conversationId, workDto));
//    }

    @MessageMapping("/chat/{conversationId}")
    public void sendChat(@RequestBody Chat chat, @DestinationVariable String conversationId, @Header("Authorization") String jwtToken){
        simpMessagingTemplate.convertAndSend("/topic/chat/" + conversationId , new MessageDto("Conversation",conversationService.sendChatting(chat, conversationId, jwtToken)));
        conversationService.sendTotalSeenCountToReceiver(jwtToken, conversationId);
    }

    @MessageMapping("/work/{conversationId}")
    public void sendWork(@RequestBody WorkDto workDto, @DestinationVariable String conversationId, @Header("Authorization") String jwtToken) throws JsonProcessingException{
        simpMessagingTemplate.convertAndSend("/topic/chat/" + conversationId , new MessageDto("Conversation",conversationService.sendWork(conversationId, workDto, jwtToken)));
        conversationService.sendTotalSeenCountToReceiver(jwtToken, conversationId);
    }

    /**
     * 테스트 해보고 지우기
     * @param chat
     * @param conversationId
     * @param request
     */

    @PostMapping("/api/conversation/chat/{conversationId}")
    public void sendChatTest(@RequestBody Chat chat, @PathVariable String conversationId, HttpServletRequest request){
        simpMessagingTemplate.convertAndSend("/topic/chat/" + conversationId , new MessageDto("Conversation", new ConversationDto(conversationService.sendChatting1(chat, conversationId, request))));
    }


    @MessageMapping("/card/{conversationId}")
    public void sendCard(@RequestBody Chat chat, @DestinationVariable String conversationId, @Header("Authorization") String jwtToken){
        simpMessagingTemplate.convertAndSend("/topic/chat/"+conversationId , new MessageDto("Conversation", conversationService.sendCard(chat, conversationId, jwtToken)));
        conversationService.sendTotalSeenCountToReceiver(jwtToken, conversationId);
    }

    @GetMapping("/api/conversations")
    public List<ConversationDto> getConversations(HttpServletRequest request){
        return conversationService.getConversations(request);
    }

    @MessageMapping("/pub/conversation/{participantId}")
    public void getConversations(@RequestBody WorkDto workDto ,@PathVariable Long participantId, HttpServletRequest request){
        simpMessagingTemplate.convertAndSend("/queue/"+participantId, new ConversationDto(conversationService.openAndMessage(request, participantId,workDto)));
    }

    @PostMapping("/api/conversation/seen/{conversationId}")
    public ConversationDto setSeenConversationCount(@PathVariable String conversationId, HttpServletRequest request){
        ConversationDto conversationDto = conversationService.setConversationSeenCount(request, conversationId);
        conversationService.sendTotalSeenCount(request);
        return conversationDto;
    }

    @GetMapping("/api/conversation/seen")
    public int getSeenCount(HttpServletRequest request){
        return conversationService.sendTotalSeenCount(request);
    }

    @GetMapping("/api/conversation/{conversationId}")
    public ConversationDto getConversation(@PathVariable String conversationId, HttpServletRequest request){
        return conversationService.getConversation(conversationId, request);
    }

}