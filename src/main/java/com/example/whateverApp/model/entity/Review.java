package com.example.whateverApp.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    @OneToOne
    private Work work;

    //private User
    @NotNull(message = "별점을 적어주세요")
    private int rating;
    private String body;
}
