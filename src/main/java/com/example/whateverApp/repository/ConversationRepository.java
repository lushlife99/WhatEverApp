package com.example.whateverApp.repository;

import com.example.whateverApp.model.document.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation, String> {

    Optional<Conversation> findByCreatorIdAndParticipantId(Long creatorId, Long participatorId);
}
