package com.example.whateverApp.dto;

import com.example.whateverApp.model.document.Chat;
import com.example.whateverApp.model.document.Conversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationDto {

    private String _id;
    private Long creatorId;
    private Long participantId;
    private String creatorName;
    private String participatorName;
    private Long workId;
    private List<Chat> chatList = new ArrayList<>();
    private LocalDateTime updatedAt;
    private Boolean finished;
    private int seenCountByCreator;
    private int seenCountByParticipator;

    public ConversationDto(Conversation conversation){
        this._id = conversation.get_id();
        this.creatorId = conversation.getCreatorId();
        this.participantId = conversation.getParticipantId();
        this.creatorName = conversation.getCreatorName();
        this.participatorName = conversation.getParticipatorName();
        this.workId = conversation.getWorkId();
        this.chatList = conversation.getChatList();
        this.updatedAt = conversation.getUpdatedAt();
        this.finished = conversation.getFinished();
        this.seenCountByCreator = conversation.getSeenCountByCreator();
        this.seenCountByParticipator = conversation.getSeenCountByParticipator();
    }
}
