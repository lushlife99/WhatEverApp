package com.example.whateverApp.model.document;

import com.example.whateverApp.model.entity.Work;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("chats")
public class Chat {

    @Id
    private String _id;
    private String messageType;
    private String message;
    private String senderName;
    private String receiverName;
    @OneToOne
    private Work work;
    @CreatedDate
    private LocalDateTime sendTime;
}
