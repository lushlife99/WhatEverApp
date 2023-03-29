package com.example.whateverApp.service;

import com.example.whateverApp.dto.TokenInfo;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.document.Chat;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.ChatRepository;
import com.example.whateverApp.repository.ConversationRepository;
import com.example.whateverApp.repository.UserRepository;
import com.example.whateverApp.service.interfaces.ConversationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.bson.json.JsonObject;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class ConversationImpl implements ConversationService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ChatRepository chatRepository;
    @Override
    public Conversation open(HttpServletRequest request, Long participatorId) {
        String accessToken = request.getHeader("Authorization").substring(7);
        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
        User creator = userRepository.findByUserId(authentication.getName()).get();
        Conversation conversation = new Conversation();
        conversation.setCreator_id(creator.getId());
        conversation.setParticipant_id(participatorId);
        return conversationRepository.save(conversation);
    }



    @Override
    public Conversation sendWork(HttpServletRequest request, String conversationId, Work work) {
        Chat chat = new Chat();
        chat.setMessageType("Work");
        Authentication authentication = jwtTokenProvider.getAuthentication(request.getHeader("Authorization").substring(7));
        User sender = userRepository.findByUserId(authentication.getName()).get();
        chat.setSenderName(sender.getName());
        Conversation conversation = conversationRepository.findById(conversationId).get();
        User participant = userRepository.findById(conversation.getParticipant_id()).get();
        chat.setReceiverName(participant.getName());
        chat.setWork(work);
        conversation.updateChat(chat);
        Chat save = chatRepository.save(chat);
        return conversationRepository.save(conversation);
    }

    @Override
    public Conversation sendChatting(HttpServletRequest request, String conversationId, String message) {
        Chat chat = new Chat();
        chat.setMessageType("Message");
        Authentication authentication = jwtTokenProvider.getAuthentication(request.getHeader("Authorization").substring(7));
        User sender = userRepository.findByUserId(authentication.getName()).get();
        chat.setSenderName(sender.getName());
        Conversation conversation = conversationRepository.findById(conversationId).get();
        User participant = userRepository.findById(conversation.getParticipant_id()).get();
        chat.setReceiverName(participant.getName());
        chat.setMessage(message);
        conversation.updateChat(chat);
        Chat save = chatRepository.save(chat);
        return conversationRepository.save(conversation);
    }



}
