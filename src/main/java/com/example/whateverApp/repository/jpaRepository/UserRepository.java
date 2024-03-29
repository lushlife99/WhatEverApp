package com.example.whateverApp.repository.jpaRepository;

import com.example.whateverApp.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(String userId);
    Optional<User> findByRefreshToken(String refreshToken);
    Optional<User> findByUserIdAndPassword(String userId, String password);
}
