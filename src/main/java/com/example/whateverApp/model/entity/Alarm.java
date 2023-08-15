package com.example.whateverApp.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alarm {

    @Id @GeneratedValue
    private Long id;
    private String title;
    private String body;

    @ManyToOne
    @JoinColumn(name = "user")
    @JsonIgnore
    private User user;


    private Boolean seen = false;

    @CreationTimestamp
    private LocalDateTime createdTime;

}
