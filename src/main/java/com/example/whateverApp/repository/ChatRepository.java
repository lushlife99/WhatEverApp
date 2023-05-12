package com.example.whateverApp.repository;

import com.example.whateverApp.model.document.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;

public interface ChatRepository extends MongoRepository<Chat, String> {
}
