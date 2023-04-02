package com.example.whateverApp.service.interfaces;

import com.example.whateverApp.model.document.Chat;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import jakarta.servlet.http.HttpServletRequest;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface ConversationService {

    Conversation openAndMessage(HttpServletRequest request, Long participatorId);
    Conversation sendWork(HttpServletRequest request, String conversationId, Work work); //채팅으로 일을 의뢰함.
    Conversation sendChatting(Chat chat, String conversationId);
}
