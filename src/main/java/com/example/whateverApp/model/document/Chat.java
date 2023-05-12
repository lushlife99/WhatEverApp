package com.example.whateverApp.model.document;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

@Data
@Document("chats")
public class Chat {

    @Id
    private String _id;
    private String messageType;
    private String message;
    private String senderName;
    private String receiverName;

    @CreatedDate
    private LocalDateTime sendTime = LocalDateTime.now();
}
