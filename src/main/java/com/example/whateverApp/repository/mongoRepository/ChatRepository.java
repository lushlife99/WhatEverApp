package com.example.whateverApp.repository.mongoRepository;

import com.example.whateverApp.model.document.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;

public interface ChatRepository extends MongoRepository<Chat, String> {
}
