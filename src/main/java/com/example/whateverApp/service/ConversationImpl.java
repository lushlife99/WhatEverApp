package com.example.whateverApp.service;

import com.example.whateverApp.dto.MessageDto;
import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.document.Chat;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.mongoRepository.ChatRepository;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.example.whateverApp.repository.jpaRepository.WorkRepository;
import com.example.whateverApp.repository.mongoRepository.ConversationRepository;
import com.example.whateverApp.service.interfaces.ConversationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
        User creator = jwtTokenProvider.getUser(request)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Work work = workRepository.findById(workDto.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
        User participator = userRepository.findById(participatorId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        work.setHelper(participator);

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
    public Conversation sendWork(String conversationId, WorkDto work1, String jwtToken) throws JsonProcessingException {
        User sender = userRepository.findByUserId(jwtTokenProvider.getAuthentication(jwtToken.substring(7)).getName())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Work work = workRepository.findById(work1.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));


        Chat chat = new Chat();
        chat.setMessageType("Work");
        chat.setMessage(mapper.writeValueAsString(work1));
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
        User receiver;

        if(conversation.getCreatorId().equals(sender.getId()))
            receiver = userRepository.findById(conversation.getParticipantId()).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        else
            receiver = userRepository.findById(conversation.getCreatorId()).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));


        conversation.setWorkId(work.getId());

        chat.setSenderName(sender.getName());
        chat.setReceiverName(receiver.getName());
        conversation.updateChat(chat);
        chatRepository.save(chat);
        return conversationRepository.save(conversation);
    }

    public List<Conversation> getConversations(HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return conversationRepository.findAll().stream().
                filter(c -> user.getId().equals(c.getParticipantId()) || user.getId().equals(c.getCreatorId())).toList();
    }

    @Override
    public Conversation sendChatting(Chat chat, String conversationId) {

        Conversation conversation = conversationRepository.findById(conversationId).
                orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
        chat.setMessageType("Chat");
        conversation.updateChat(chat);
        chatRepository.save(chat);
        return conversationRepository.save(conversation);
    }

    public Conversation sendCard(Chat chat, String conversationId){

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
        chat.setMessageType("Card");
        conversation.updateChat(chat);
        chatRepository.save(chat);
        return conversationRepository.save(conversation);
    }
}
