package com.example.whateverApp.dto;

import com.example.whateverApp.model.entity.Review;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {

    private Long id;
    private Long userId;
    @Min(value = 1, message = "별점 ")
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
