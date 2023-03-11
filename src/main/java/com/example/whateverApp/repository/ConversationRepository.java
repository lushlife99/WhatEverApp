package com.example.whateverApp.repository;

import com.example.whateverApp.model.document.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConversationRepository extends MongoRepository<Conversation, String> {
}
