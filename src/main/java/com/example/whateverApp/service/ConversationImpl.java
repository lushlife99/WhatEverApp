package com.example.whateverApp.service;

import com.example.whateverApp.dto.MessageDto;
import com.example.whateverApp.dto.TokenInfo;
import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.document.Chat;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.ChatRepository;
import com.example.whateverApp.repository.ConversationRepository;
import com.example.whateverApp.repository.UserRepository;
import com.example.whateverApp.repository.WorkRepository;
import com.example.whateverApp.service.interfaces.ConversationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.bson.json.JsonObject;
import org.bson.json.JsonWriter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


@RequiredArgsConstructor
@Service
public class ConversationImpl implements ConversationService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final WorkRepository workRepository;
    private final ObjectMapper mapper;
    @Override
    public Conversation openAndMessage(HttpServletRequest request, Long participatorId, WorkDto workDto) {
        String accessToken = request.getHeader("Authorization").substring(7);
        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
        User creator = userRepository.findByUserId(authentication.getName()).get();
        Conversation conversation = open(creator.getId(), participatorId);
        Work work = workRepository.findById(workDto.getId()).get();
        work.setHelper(userRepository.findById(participatorId).get());
        simpMessagingTemplate.convertAndSend("/queue/" + creator.getId() , new MessageDto("OpenChat", conversation));
        simpMessagingTemplate.convertAndSend("/queue/" + participatorId , new MessageDto("OpenChat", conversation));
        return conversation;
    }

    @Transactional
    public Conversation open(Long creatorId, Long participatorId){
        Optional<Conversation> findConv = conversationRepository.findByCreatorIdAndParticipantId(creatorId, participatorId);
        if(findConv.isPresent()){
            return findConv.get();
        }
        Conversation conversation = new Conversation();
        conversation.setCreatorId(creatorId);
        conversation.setParticipantId(participatorId);
        return conversationRepository.save(conversation);
    }


    @Override
    @Transactional
    public Conversation sendWork(String conversationId, WorkDto work1) throws JsonProcessingException {
        Work work = workRepository.findById(work1.getId()).get();
        Chat chat = new Chat();
        chat.setMessageType("Work");
        chat.setMessage(mapper.writeValueAsString(work1));
        System.out.println(chat.getMessage());
        User sender = userRepository.findById(work1.getCustomerId()).get();
        Conversation conversation = conversationRepository.findById(conversationId).get();
        conversation.setWorkId(work.getId());
        User participant = userRepository.findById(conversation.getParticipantId()).get();
        chat.setSenderName(sender.getName());
        chat.setReceiverName(participant.getName());
        conversation.updateChat(chat);
        chatRepository.save(chat);
        return conversationRepository.save(conversation);
    }

    @Override
    public Conversation sendChatting(Chat chat, String conversationId) {

        Conversation conversation = conversationRepository.findById(conversationId).get();
        chat.setMessageType("Chat");
        conversation.updateChat(chat);
        return conversationRepository.save(conversation);
    }

}
