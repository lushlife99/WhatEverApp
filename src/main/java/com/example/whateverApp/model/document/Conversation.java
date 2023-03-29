package com.example.whateverApp.model.document;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document("conversations")
public class Conversation {

    @Id
    private String _id;
    private Long creator_id;
    private Long participant_id;
    private List<Chat> chatList = new ArrayList<>();

    public List<Chat> updateChat(Chat chat){
        chatList.add(chat);
        return  chatList;
    }
}
