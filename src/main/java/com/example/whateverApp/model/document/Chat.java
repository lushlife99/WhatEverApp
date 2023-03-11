package com.example.whateverApp.model.document;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("chats")
public class Chat {

    @Id
    private String _id;
    private String message;
    private String senderName;
    private String receiverName;
    @CreatedDate
    private LocalDateTime sendTime;
}
