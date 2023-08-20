package com.example.whateverApp.service;

import com.example.whateverApp.dto.ConversationDto;
import com.example.whateverApp.dto.MessageDto;
import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.WorkProceedingStatus;
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

import java.util.ArrayList;
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

    /**
     *
     * @param request
     * @param participatorId
     * @param workDto
     * @return conversation
     * message to
     */
    @Override
    public Conversation openAndMessage(HttpServletRequest request, Long participatorId, WorkDto workDto) {
        User creator = jwtTokenProvider.getUser(request)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Work work = workRepository.findById(workDto.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
        User participator = userRepository.findById(participatorId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if(!work.getProceedingStatus().equals(WorkProceedingStatus.CREATED)) //채팅방을 열 시점에, work가 이미 다른사람과 체결된 경우
            throw new CustomException(ErrorCode.ALREADY_PROCEED_WORK);


        List<Conversation> findConvList = conversationRepository.findAll().stream()
                .filter(c -> c.getCreatorId().equals(creator.getId()) || c.getParticipantId().equals(creator.getId()))
                .filter(c -> c.getCreatorId().equals(participator.getId()) || c.getParticipantId().equals(participator.getId())).toList();

        for (Conversation conversation : findConvList) {
            if(conversation.getFinished().equals(Boolean.FALSE))
                throw new CustomException(ErrorCode.OTHER_WORK_IS_PROCEEDING);
        }

        Conversation conversation = open(creator, participator);
        List<ConversationDto> convByCreator = getConversations(request);
        List<Conversation> convByParticipator = conversationRepository.findAll().stream()
                .filter( c-> c.getCreatorId().equals(participator.getId()) || c.getParticipantId().equals(participator.getId()))
                .filter( c -> c.getFinished().equals(Boolean.FALSE)).toList();
        simpMessagingTemplate.convertAndSend("/queue/" + creator.getId() , new MessageDto("OpenChat", convByCreator));
        simpMessagingTemplate.convertAndSend("/queue/" + participatorId , new MessageDto("OpenChat", convByParticipator));
        return conversation;
    }

    @Transactional
    public Conversation open(User creator, User participator){

        Conversation conversation = new Conversation();
        conversation.setCreatorId(creator.getId());
        conversation.setParticipantId(participator.getId());
        conversation.setCreatorName(creator.getName());
        conversation.setParticipatorName(participator.getName());
        conversation.setSeenCountByParticipator(0);
        conversation.setSeenCountByParticipator(0);
        conversation.setFinished(false);
        return conversationRepository.save(conversation);
    }


    @Override
    @Transactional
    public Conversation sendWork(String conversationId, WorkDto workDto, String jwtToken) throws JsonProcessingException {

        Work work = workRepository.findById(workDto.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));

        Chat chat = Chat.builder()
                .messageType("Work")
                .message(mapper.writeValueAsString(workDto))
                .senderName(conversation.getCreatorName())
                .receiverName(conversation.getParticipatorName())
                .build();

        conversation.updateChat(chat);
        chatRepository.save(chat);
        return conversationRepository.save(conversation);
    }

    public List<ConversationDto> getConversations(HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Conversation> list = conversationRepository.findAll().stream()
                .filter(c -> user.getId().equals(c.getParticipantId()) || user.getId().equals(c.getCreatorId()))
                .filter(c -> c.getFinished().equals(Boolean.FALSE)).toList();
        List<ConversationDto> conversationDtoList = new ArrayList<>();

        for (Conversation conversation : list)
            conversationDtoList.add(new ConversationDto(conversation));
        return conversationDtoList;
    }

    @Override
    public Conversation sendChatting(Chat chat, String conversationId, String jwtToken) {

        Conversation conversation = conversationRepository.findById(conversationId).
                orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
        chat.setMessageType("Chat");
        conversation.updateChat(chat);
        chatRepository.save(chat);

        return conversationRepository.save(conversation);

    }

    public Conversation sendCard(Chat chat, String conversationId, String jwtToken){

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
        chat.setMessageType("Card");
        conversation.updateChat(chat);
        chatRepository.save(chat);
        return conversationRepository.save(conversation);
    }

    public void setConversationSeenCount(HttpServletRequest request, String conversationId){
        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));

        if(conversation.getCreatorId().equals(user.getId())){
            conversation.setSeenCountByCreator(conversation.getChatList().size());
        } else if(conversation.getParticipantId().equals(user.getId())){
            conversation.setSeenCountByParticipator(conversation.getChatList().size());
        } else throw new CustomException(ErrorCode.BAD_REQUEST);

    }

    public int sendTotalSeenCount(User user){
        Optional<List<Conversation>> createdConv = conversationRepository.findByCreatorId(user.getId());
        Optional<List<Conversation>> participatedConv = conversationRepository.findByParticipantId(user.getId());
        int totalSeenCount = 0;
        if(createdConv.isPresent()){
            List<Conversation> conversationList = createdConv.get();
            for (Conversation conversation : conversationList) {
                totalSeenCount += conversation.getChatList().size() - conversation.getSeenCountByCreator();
            }
        }

        if(participatedConv.isPresent()){
            List<Conversation> conversationList = participatedConv.get();
            for (Conversation conversation : conversationList) {
                totalSeenCount += conversation.getChatList().size() - conversation.getSeenCountByParticipator();
            }
        }

        return totalSeenCount;
    }
}
