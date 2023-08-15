package com.example.whateverApp.service;

import com.example.whateverApp.dto.ReviewDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.entity.Review;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.repository.jpaRepository.ReviewRepository;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public List<ReviewDto> getReviewList(HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        List<Review> reviewList = user.getReviewList();

        List<ReviewDto> reviewDtos = new ArrayList<>();
        for (Review review : reviewList) {
            reviewDtos.add(new ReviewDto(review));
        }

        return reviewDtos;
    }

    public List<ReviewDto> getHelperReviews(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Review> reviewList = user.getReviewList();

        List<ReviewDto> reviewDtos = new ArrayList<>();
        for (Review review : reviewList) {
            reviewDtos.add(new ReviewDto(review));
        }

        return reviewDtos;
    }


}
