package com.example.whateverApp.service;

import com.example.whateverApp.dto.MessageDto;
import com.example.whateverApp.dto.TokenInfo;
import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.document.Chat;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.*;
import com.example.whateverApp.service.interfaces.ConversationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
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
        Work work = workRepository.findById(workDto.getId()).get();
        work.setHelper(userRepository.findById(participatorId).get());
        User participator = userRepository.findById(participatorId).get();
        Conversation conversation = open(creator, participator, work.getId());
        List<Conversation> conversationList = conversationRepository.findAll().stream().filter( c->{
            return c.getCreatorId().equals(creator.getId()) || c.getParticipantId().equals(creator.getId());
        }).toList();
        simpMessagingTemplate.convertAndSend("/queue/" + creator.getId() , new MessageDto("OpenChat", conversationList));
        simpMessagingTemplate.convertAndSend("/queue/" + participatorId , new MessageDto("OpenChat", conversationList));
        return conversation;

    }

    @Transactional
    public Conversation open(User creator, User participator, Long workId){
        Optional<Conversation> findConv = conversationRepository.findByCreatorIdAndParticipantId(creator.getId(), participator.getId());
        if(findConv.isPresent()){
            /**
             * 나중에 수정하기.
             * work가 이미 진행중이면 예외처리해주고
             * work가 만약 끝났다면 work를 새로운 work로 set해주기.
             */
            Conversation conversation = findConv.get();
            return findConv.get();
        }
        Conversation conversation = new Conversation();
        conversation.setCreatorId(creator.getId());
        conversation.setParticipantId(participator.getId());
        conversation.setWorkId(workId);
        conversation.setCreatorName(creator.getName());
        conversation.setParticipatorName(participator.getName());
        return conversationRepository.save(conversation);
    }


    @Override
    @Transactional
    public Conversation sendWork(String conversationId, WorkDto work1) throws JsonProcessingException {
        Work work = workRepository.findById(work1.getId()).get();
        Chat chat = new Chat();
        chat.setMessageType("Work");
        chat.setMessage(mapper.writeValueAsString(work1));
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

    public List<Conversation> getConversations(HttpServletRequest request){
        String userId = jwtTokenProvider.getAuthentication(jwtTokenProvider.resolveToken(request)).getName();
        User user = userRepository.findByUserId(userId).get();
        return conversationRepository.findAll().stream().filter(c -> user.getId().equals(c.getParticipantId())
        || user.getId().equals(c.getCreatorId())).toList();
    }

    @Override
    public Conversation sendChatting(Chat chat, String conversationId) {

        Conversation conversation = conversationRepository.findById(conversationId).get();
        chat.setMessageType("Chat");
        conversation.updateChat(chat);
        chatRepository.save(chat);
        return conversationRepository.save(conversation);
    }

    public Conversation sendCard(Chat chat, String conversationId){

        Conversation conversation = conversationRepository.findById(conversationId).get();
        chat.setMessageType("Card");
        conversation.updateChat(chat);
        return conversationRepository.save(conversation);
    }

}
