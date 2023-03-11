package com.example.whateverApp.model.document;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document("conversations")
public class Conversation {

    @Id
    private String _id;
    private Integer creator_id;
    private Integer participant_id;
    private List<Chat> chatList;
}
