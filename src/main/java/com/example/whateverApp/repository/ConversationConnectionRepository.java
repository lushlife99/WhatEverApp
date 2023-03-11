package com.example.whateverApp.repository;

import com.example.whateverApp.model.entity.ConversationConnection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationConnectionRepository extends JpaRepository<ConversationConnection, Long> {
}
