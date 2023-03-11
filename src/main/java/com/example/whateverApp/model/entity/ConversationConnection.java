package com.example.whateverApp.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Data
@Entity
public class ConversationConnection {

    @Id @GeneratedValue
    private Long id;

    @OneToOne
    private User creator;
    @OneToOne
    private User participator;


}
