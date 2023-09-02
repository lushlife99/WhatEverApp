package com.example.whateverApp.repository.mongoRepository;

import com.example.whateverApp.model.document.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation, String> {


    Optional<Conversation> findByWorkId(Long workId);



}
