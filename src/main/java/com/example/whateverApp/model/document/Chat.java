package com.example.whateverApp.model.document;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

@Data
@Document("chats")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chat {

    @Id
    private String _id;
    private String messageType;
    private String message;
    private String senderName;
    private String receiverName;

    private LocalDateTime sendTime = LocalDateTime.now();
}
