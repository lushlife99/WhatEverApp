package com.example.whateverApp.service;

import com.example.whateverApp.dto.ConversationDto;
import com.example.whateverApp.dto.MessageDto;
import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.AccountStatus;
import com.example.whateverApp.model.WorkProceedingStatus;
import com.example.whateverApp.model.document.Chat;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.mongoRepository.ChatRepository;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.example.whateverApp.repository.jpaRepository.WorkRepository;
import com.example.whateverApp.repository.mongoRepository.ConversationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
@Service
public class ConversationImpl{

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
    public Conversation openAndMessage(HttpServletRequest request, Long participatorId, WorkDto workDto) {
        User creator = jwtTokenProvider.getUser(request)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Work work = workRepository.findById(workDto.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
        User participator = userRepository.findById(participatorId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if(!work.getProceedingStatus().equals(WorkProceedingStatus.CREATED))
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
        if(creator.getAccountStatus().equals(AccountStatus.WILL_BAN))
            throw new CustomException(ErrorCode.WILL_BANNED_ACCOUNT);

        if(participator.getAccountStatus().equals(AccountStatus.WILL_BAN))
            throw new CustomException(ErrorCode.PARTICIPATOR_ACCOUNT_WILL_BAN);

        Conversation conversation = new Conversation();
        conversation.setCreatorId(creator.getId());
        conversation.setParticipantId(participator.getId());
        conversation.setCreatorName(creator.getName());
        conversation.setParticipatorName(participator.getName());
        conversation.setSeenCountByParticipator(0);
        conversation.setSeenCountByParticipator(0);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setFinished(false);
        return conversationRepository.save(conversation);
    }

    public void delete(String conversationId){
        conversationRepository.deleteById(conversationId);
        simpMessagingTemplate.convertAndSend("/topic/chat/"+conversationId, new MessageDto("DeleteConv", conversationId));
    }

    public ConversationDto getConversation(String conversationId, HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));

        if(!conversation.getCreatorId().equals(user.getId()) && !conversation.getParticipantId().equals(user.getId())) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        if(conversation.getFinished())
            throw new CustomException(ErrorCode.FINISHED_CONVERSATION);

        return new ConversationDto(conversation);
    }


    @Transactional
    public ConversationDto sendWork(String conversationId, WorkDto workDto, String jwtToken) throws JsonProcessingException {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));

        Chat chat = Chat.builder()
                .messageType("Work")
                .message(mapper.writeValueAsString(workDto))
                .senderName(conversation.getCreatorName())
                .receiverName(conversation.getParticipatorName())
                .sendTime(LocalDateTime.now())
                .build();

        updateConv(conversation, chat, "Work");
        ConversationDto conversationDto = setConversationSeenCount(jwtToken, conversation);
        return conversationDto;
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

    @Transactional
    public ConversationDto sendChatting(Chat chat, String conversationId, String jwtToken) {
        Conversation conversation = conversationRepository.findById(conversationId).
                orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));

        updateConv(conversation, chat, "Chat");
        ConversationDto conversationDto = setConversationSeenCount(jwtToken, conversation);
        return conversationDto;

    }


    @Transactional
    public ConversationDto sendCard(Chat chat, String conversationId, String jwtToken){
        Conversation conversation = conversationRepository.findById(conversationId).
                orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));

        updateConv(conversation, chat, "Card");
        ConversationDto conversationDto = setConversationSeenCount(jwtToken, conversation);
        return conversationDto;
    }

    public ConversationDto setConversationSeenCount(HttpServletRequest request, String conversationId){
        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));

        if(conversation.getCreatorId().equals(user.getId())){
            conversation.setSeenCountByCreator(conversation.getChatList().size());
        } else if(conversation.getParticipantId().equals(user.getId())){
            conversation.setSeenCountByParticipator(conversation.getChatList().size());
        } else throw new CustomException(ErrorCode.BAD_REQUEST);

        return new ConversationDto(conversationRepository.save(conversation));
    }

    public ConversationDto setConversationSeenCount(String jwtToken, Conversation conversation){
        User user = userRepository.findByUserId(jwtTokenProvider.getAuthentication(jwtToken.substring(7)).getName()).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if(conversation.getCreatorId().equals(user.getId())){
            conversation.setSeenCountByCreator(conversation.getChatList().size());
        } else if(conversation.getParticipantId().equals(user.getId())){
            conversation.setSeenCountByParticipator(conversation.getChatList().size());
        } else throw new CustomException(ErrorCode.BAD_REQUEST);

        return new ConversationDto(conversationRepository.save(conversation));
    }



    public int sendTotalSeenCount(HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        List<Conversation> list = conversationRepository.findAll().stream()
                .filter(c -> c.getFinished().equals(Boolean.FALSE))
                .filter(c -> c.getCreatorId().equals(user.getId()) || c.getParticipantId().equals(user.getId())).toList();

        int totalSeenCount = 0;
        for (Conversation conversation : list) {
            if(conversation.getCreatorId().equals(user.getId()))
                totalSeenCount += conversation.getChatList().size() - conversation.getSeenCountByCreator();

            else totalSeenCount += conversation.getChatList().size() - conversation.getSeenCountByParticipator();
        }
        simpMessagingTemplate.convertAndSend("/queue/" + user.getId() , new MessageDto("SetConvSeenCount", totalSeenCount));
        return totalSeenCount;
    }

    public int sendTotalSeenCountToReceiver(String jwtToken, String conversationId){
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
        User requestUser = userRepository.findByUserId(jwtTokenProvider.getAuthentication(jwtToken.substring(7)).getName()).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Long receiverId  = 0L;
        if(conversation.getCreatorId().equals(requestUser.getId()))
            receiverId = conversation.getParticipantId();

        else receiverId = conversation.getCreatorId();

        User user = userRepository.findById(receiverId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Conversation> list = conversationRepository.findAll().stream()
                .filter(c -> c.getFinished().equals(Boolean.FALSE))
                .filter(c -> c.getCreatorId().equals(user.getId()) || c.getParticipantId().equals(user.getId())).toList();

        List<ConversationDto> convDtoList = new ArrayList<>();
        int totalSeenCount = 0;
        for (Conversation receiverConv : list) {
            if(conversation.getCreatorId().equals(user.getId()))
                totalSeenCount += receiverConv.getChatList().size() - receiverConv.getSeenCountByCreator();

            else totalSeenCount += receiverConv.getChatList().size() - receiverConv.getSeenCountByParticipator();
        }
        for (Conversation receiverConv : list)
            convDtoList.add(new ConversationDto(receiverConv));

        simpMessagingTemplate.convertAndSend("/queue/" + user.getId() , new MessageDto("SetConvSeenCount", totalSeenCount));
        return totalSeenCount;
    }
    public Conversation updateConv(Conversation conversation, Chat chat, String messageType){
        chat.setMessageType(messageType);
        conversation.updateChat(chat);
        chatRepository.save(chat);
        return conversationRepository.save(conversation);
    }
}