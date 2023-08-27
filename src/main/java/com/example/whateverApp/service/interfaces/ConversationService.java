package com.example.whateverApp.service.interfaces;

import com.example.whateverApp.dto.ConversationDto;
import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.model.document.Chat;
import com.example.whateverApp.model.document.Conversation;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;

public interface ConversationService {

    Conversation openAndMessage(HttpServletRequest request, Long participatorId, WorkDto workDto);
    ConversationDto sendWork(String conversationId, WorkDto work, String jwtToken) throws JsonProcessingException; //채팅으로 일을 의뢰함.
    ConversationDto sendChatting(Chat chat, String conversationId, String jwtToken);
}
