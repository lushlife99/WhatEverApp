package com.example.whateverApp.repository;

import com.example.whateverApp.model.entity.Review;
import com.example.whateverApp.model.entity.Work;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByWork(Work work);
}
