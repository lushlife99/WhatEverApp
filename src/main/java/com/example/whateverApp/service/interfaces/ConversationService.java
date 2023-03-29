package com.example.whateverApp.service.interfaces;

import com.example.whateverApp.model.document.Chat;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import jakarta.servlet.http.HttpServletRequest;

public interface ConversationService {

    Conversation open(HttpServletRequest request, Long participatorId);
    Conversation sendWork(HttpServletRequest request, String conversationId, Work work); //채팅으로 일을 의뢰함.
    Conversation sendChatting(Chat chat, String conversationId);
}
