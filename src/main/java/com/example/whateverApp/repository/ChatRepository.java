package com.example.whateverApp.repository;

import com.example.whateverApp.model.document.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRepository extends MongoRepository<Chat, String> {
}
