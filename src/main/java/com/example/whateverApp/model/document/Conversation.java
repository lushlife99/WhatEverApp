package com.example.whateverApp.model.document;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Document("conversations")
public class Conversation {

    @Id
    private String _id;
    private Long creatorId;
    private Long participantId;
    private String creatorName;
    private String participatorName;
    private Long workId;
    private List<Chat> chatList = new ArrayList<>();

    private int seenCountByCreator;
    private int seenCountByParticipator;
    private Boolean finished = false;

    @DateTimeFormat(pattern = "yy-MM-dd HH:mm")
    private LocalDateTime updatedAt;

    public List<Chat> updateChat(Chat chat){
        chatList.add(chat);
        setUpdatedAt(LocalDateTime.now().plusHours(9));
        return  chatList;
    }
}
