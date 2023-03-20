package com.example.whateverApp.repository;

import com.example.whateverApp.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(String userId);
    Optional<User> findByRefreshToken(String refreshToken);

}
