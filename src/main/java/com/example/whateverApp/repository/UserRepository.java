package com.example.whateverApp.repository;

import com.example.whateverApp.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(String userId);
    Optional<User> findByRefreshToken(String refreshToken);

    Page<User> findAll(Pageable pageable);

    Optional<User> findByUserIdAndPassword(String userId, String password);
}
