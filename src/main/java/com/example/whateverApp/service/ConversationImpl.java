package com.example.whateverApp.service;

import com.example.whateverApp.dto.ConversationDto;
import com.example.whateverApp.dto.MessageDto;
import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.AccountStatus;
import com.example.whateverApp.model.MessageType;
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
import org.jetbrains.annotations.NotNull;
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
    private final String queuePrefix = "/queue/";
    private final String topicPrefix = "/topic/chat/";

    public Conversation openConv(HttpServletRequest request, Long participatorId, WorkDto workDto) {
        User creator = jwtTokenProvider.getUser(request);
        Work work = workRepository.findById(workDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
        User participator = userRepository.findById(participatorId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if(!work.getProceedingStatus().equals(WorkProceedingStatus.CREATED))
            throw new CustomException(ErrorCode.ALREADY_PROCEED_WORK);
        if(creator.getAccountStatus().equals(AccountStatus.WILL_BAN))
            throw new CustomException(ErrorCode.WILL_BANNED_ACCOUNT);
        if(participator.getAccountStatus().equals(AccountStatus.WILL_BAN))
            throw new CustomException(ErrorCode.PARTICIPATOR_ACCOUNT_WILL_BAN);
        if(isExistConv(creator,participator))
            throw new CustomException(ErrorCode.OTHER_WORK_IS_PROCEEDING);

        Conversation conversation = createConv(creator, participator);
        simpMessagingTemplate.convertAndSend(queuePrefix + creator.getId() , new MessageDto(MessageType.OpenChat.getDetail(), conversation));
        simpMessagingTemplate.convertAndSend(queuePrefix + participatorId , new MessageDto(MessageType.OpenChat.getDetail(), conversation));
        return conversation;
    }

    @NotNull
    private Boolean isExistConv(User creator, User participator) {
        List<Conversation> findConvList = conversationRepository.findAll().stream()
                .filter(c -> c.getFinished() == false)
                .filter(c -> c.getCreatorId().equals(creator.getId()) || c.getParticipantId().equals(creator.getId()))
                .filter(c -> c.getCreatorId().equals(participator.getId()) || c.getParticipantId().equals(participator.getId())).toList();

        if(findConvList.size() > 0)
            return true;

        return false;
    }

    public Conversation createConv(User creator, User participator){

        return conversationRepository.save(Conversation.builder()
                .creatorId(creator.getId())
                .participantId(participator.getId())
                .creatorName(creator.getName())
                .participatorName(participator.getName())
                .seenCountByCreator(1)
                .seenCountByParticipator(0)
                .chatList(new ArrayList<Chat>())
                .createdAt(LocalDateTime.now().plusHours(9))
                .finished(false).build());
    }

    public void sendDelete(String conversationId){
        conversationRepository.deleteById(conversationId);
        sendStompMessage(topicPrefix+conversationId, new MessageDto(MessageType.DeleteConv.getDetail() , conversationId));
    }

    public void sendDelete(Long workId){
        Conversation conversation = conversationRepository.findByWorkId(workId).orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
        conversationRepository.deleteById(conversation.get_id());
        sendStompMessage(topicPrefix + conversation.get_id(), new MessageDto(MessageType.DeleteConv.getDetail() , conversation.get_id()));
    }

    public ConversationDto getConversation(String conversationId, HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request);
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));

        if(!conversation.getCreatorId().equals(user.getId()) && !conversation.getParticipantId().equals(user.getId()))
            throw new CustomException(ErrorCode.BAD_REQUEST);

        if(conversation.getFinished())
            throw new CustomException(ErrorCode.FINISHED_CONVERSATION);

        return new ConversationDto(conversation);
    }


    @Transactional
    public void sendWork(String conversationId, WorkDto workDto, String jwtToken) throws JsonProcessingException {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
        Chat chat = Chat.builder()
                .messageType(MessageType.Work.getDetail())
                .message(mapper.writeValueAsString(workDto))
                .senderName(conversation.getCreatorName())
                .receiverName(conversation.getParticipatorName())
                .sendTime(LocalDateTime.now().plusHours(9))
                .build();

        conversation = updateConv(conversation, chat, MessageType.Work.getDetail());
        ConversationDto conversationDto = setConversationSeenCount(jwtToken, conversation);
        sendStompMessage(topicPrefix+conversationId, new MessageDto(MessageType.Conversation.getDetail(), conversationDto));
    }

    public List<ConversationDto> getConversations(HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request);

        List<Conversation> list = conversationRepository.findAll().stream()
                .filter(c -> user.getId().equals(c.getParticipantId()) || user.getId().equals(c.getCreatorId()))
                .filter(c -> c.getFinished().equals(Boolean.FALSE)).toList();
        List<ConversationDto> conversationDtoList = new ArrayList<>();

        for (Conversation conversation : list)
            conversationDtoList.add(new ConversationDto(conversation));
        return conversationDtoList;
    }

    @Transactional
    public void sendChatting(String conversationId, Chat chat, String jwtToken) {
        Conversation conversation = conversationRepository.findById(conversationId).
                orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));

        conversation = updateConv(conversation, chat, MessageType.Chat.getDetail());
        setConversationSeenCount(jwtToken, conversation);
        sendStompMessage(topicPrefix+conversationId, new MessageDto(MessageType.Conversation.getDetail(), new ConversationDto(conversation)));
    }


    @Transactional
    public void sendCard(String conversationId, Chat chat, String jwtToken){
        Conversation conversation = conversationRepository.findById(conversationId).
                orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));

        updateConv(conversation, chat, MessageType.Card.getDetail());
        ConversationDto conversationDto = setConversationSeenCount(jwtToken, conversation);
        sendStompMessage(topicPrefix+conversationId, new MessageDto(MessageType.Conversation.getDetail(), conversationDto));
    }

    public ConversationDto setConversationSeenCount(HttpServletRequest request, String conversationId){
        User user = jwtTokenProvider.getUser(request);
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
        int totalSeenCount = 0;
        User user = jwtTokenProvider.getUser(request);
        List<Conversation> list = conversationRepository.findAll().stream()
                .filter(c -> c.getFinished().equals(Boolean.FALSE))
                .filter(c -> c.getCreatorId().equals(user.getId()) || c.getParticipantId().equals(user.getId())).toList();

        for (Conversation conversation : list) {
            if(conversation.getCreatorId().equals(user.getId()))
                totalSeenCount += conversation.getChatList().size() - conversation.getSeenCountByCreator();
            else totalSeenCount += conversation.getChatList().size() - conversation.getSeenCountByParticipator();
        }
        sendStompMessage(queuePrefix + user.getId(), new MessageDto(MessageType.SetConvSeenCount.getDetail(), totalSeenCount));
        return totalSeenCount;
    }

    public void sendTotalSeenCountToReceiver(String jwtToken, String conversationId){
        Long receiverId  = 0L;
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
        User requestUser = userRepository.findByUserId(jwtTokenProvider.getAuthentication(jwtToken.substring(7)).getName()).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if(conversation.getCreatorId().equals(requestUser.getId()))
            receiverId = conversation.getParticipantId();
        else receiverId = conversation.getCreatorId();
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        sendStompMessage(queuePrefix + receiver.getId(), new MessageDto(MessageType.SetConvSeenCount.getDetail(), getTotalSeenCount(receiver, conversation)));
    }

    public int getTotalSeenCount(User user, Conversation conversation){
        int totalSeenCount = 0;
        List<Conversation> list = conversationRepository.findAll().stream()
                .filter(c -> c.getFinished().equals(Boolean.FALSE))
                .filter(c -> c.getCreatorId().equals(user.getId()) || c.getParticipantId().equals(user.getId())).toList();
        for (Conversation receiverConv : list) {
            if(conversation.getCreatorId().equals(user.getId()))
                totalSeenCount += receiverConv.getChatList().size() - receiverConv.getSeenCountByCreator();

            else totalSeenCount += receiverConv.getChatList().size() - receiverConv.getSeenCountByParticipator();
        }
        return totalSeenCount;
    }
    public Conversation updateConv(Conversation conversation, Chat chat, String messageType){
        chat.setMessageType(messageType);
        conversation.updateChat(chat);
        chatRepository.save(chat);
        return conversationRepository.save(conversation);
    }

    private void sendStompMessage(String destination, MessageDto message){
        simpMessagingTemplate.convertAndSend(destination, message);
    }
}