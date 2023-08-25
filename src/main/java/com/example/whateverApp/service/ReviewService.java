package com.example.whateverApp.service;

import com.example.whateverApp.dto.ReviewDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.WorkProceedingStatus;
import com.example.whateverApp.model.entity.Review;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.jpaRepository.ReviewRepository;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.example.whateverApp.repository.jpaRepository.WorkRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final WorkRepository workRepository;
    private final FirebaseCloudMessageService fcmService;

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

    public void setRating(Long workId, ReviewDto reviewDto, HttpServletRequest request) throws IOException {
        User customer = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Work work = workRepository.findById(workId).orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));

        if(!work.getProceedingStatus().equals(WorkProceedingStatus.PAYED_REWORD) || !work.getCustomer().getId().equals(customer.getId()))
            throw new CustomException(ErrorCode.BAD_REQUEST);

        if(reviewRepository.findByWork(work).isPresent())
            throw new CustomException(ErrorCode.DUPLICATE_REVIEW);

        Review review = new Review();
        review.updateReview(reviewDto);
        review.setUser(customer);
        review.setWork(work);

        fcmService.sendReviewUpload(review);
        reviewRepository.save(review);
    }

}
