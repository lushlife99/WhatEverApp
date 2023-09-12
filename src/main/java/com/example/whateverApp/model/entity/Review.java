package com.example.whateverApp.model.entity;

import com.example.whateverApp.dto.ReviewDto;
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
    @JoinColumn(name = "user")
    private User user;

    @OneToOne
    private Work work;

    //private User
    private int rating;
    private String body;

}
