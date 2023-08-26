package com.example.whateverApp.repository.jpaRepository;

import com.example.whateverApp.model.entity.PaymentsInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentsInfo, Long> {
}
