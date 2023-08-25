package com.example.whateverApp.controller;

import com.example.whateverApp.dto.ReviewDto;
import com.example.whateverApp.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("")
    public List<ReviewDto> getMyReviews(HttpServletRequest request){
        return reviewService.getReviewList(request);
    }

    @GetMapping("/{userId}")
    public List<ReviewDto> getHelperReviews(@PathVariable Long userId){
        return reviewService.getHelperReviews(userId);
    }

    @PostMapping("/{workId}")
    public void setRating(@PathVariable Long workId, @RequestBody @Validated ReviewDto reviewDto, HttpServletRequest request) throws IOException {
        reviewService.setRating(workId, reviewDto, request);
    }


}
