package com.example.whateverApp.controller;

import com.example.whateverApp.dto.ReviewDto;
import com.example.whateverApp.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/review")
    public List<ReviewDto> getMyReviews(HttpServletRequest request){
        return reviewService.getReviewList(request);
    }

    @GetMapping("/review/{userId}")
    public List<ReviewDto> getHelperReviews(@PathVariable Long userId){
        return reviewService.getHelperReviews(userId);
    }


}
