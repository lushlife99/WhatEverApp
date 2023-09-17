package com.example.whateverApp.controller;

import com.example.whateverApp.dto.ConversationDto;
import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.model.document.Chat;
import com.example.whateverApp.service.ConversationImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequiredArgsConstructor
@RestController
@CrossOrigin
public class ConversationController {

    private final ConversationImpl conversationService;
    private final String AuthHeader = "Authorization";

    @PostMapping("/api/conversation/{participantId}")
    @NotNull
    public ConversationDto createChat(@RequestBody WorkDto workDto , @PathVariable Long participantId, HttpServletRequest request){
        return new ConversationDto(conversationService.openConv(request, participantId,workDto));
    }

    @MessageMapping("/chat/{conversationId}")
    public void sendChat(@RequestBody Chat chat, @DestinationVariable String conversationId, @Header(AuthHeader) String jwtToken){
        conversationService.sendChatting(conversationId, chat, jwtToken);
        conversationService.sendTotalSeenCountToReceiver(jwtToken, conversationId);
    }

    @MessageMapping("/work/{conversationId}")
    public void sendWork(@RequestBody WorkDto workDto, @DestinationVariable String conversationId, @Header(AuthHeader) String jwtToken) throws JsonProcessingException{
        conversationService.sendWork(conversationId, workDto, jwtToken);
        conversationService.sendTotalSeenCountToReceiver(jwtToken, conversationId);

    }

    @MessageMapping("/card/{conversationId}")
    public void sendCard(@RequestBody Chat chat, @DestinationVariable String conversationId, @Header(AuthHeader) String jwtToken){
        conversationService.sendCard(conversationId, chat, jwtToken);
        conversationService.sendTotalSeenCountToReceiver(jwtToken, conversationId);
    }

    @GetMapping("/api/conversations")
    public List<ConversationDto> getConversations(HttpServletRequest request){
        return conversationService.getConversations(request);
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