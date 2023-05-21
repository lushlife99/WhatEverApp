package com.example.whateverApp.repository;

import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkRepository extends JpaRepository<Work, Long> {

    List<Work> findByCustomer(User customer);
    List<Work> findByHelper(User helper);

}
