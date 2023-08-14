package com.example.whateverApp.dto;

import com.example.whateverApp.model.entity.Review;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.springframework.jmx.export.annotation.ManagedNotifications;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {

    private Long id;
    private Long userId;
    private int rating;
    private String body;
    private Long workId;

    public ReviewDto(Review review){
        this.id = review.getId();
        this.userId = review.getUser().getId();
        this.rating = review.getRating();
        this.body = review.getBody();
        this.workId = review.getWork().getId();
    }
}
