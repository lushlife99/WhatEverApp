package com.example.whateverApp.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 결제 정보 entity
 *
 * 결제 번호
 * 결제 방법
 * 주문 번호
 * 구매 번호
 * 가격
 * 구매자 주소
 * 구매자 우편
 * 심부름
 * 구매자
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@SequenceGenerator(
        name = "PAYMENTS_INFO_SEQ_GENERATOR",
        sequenceName = "PAYMENTS_INFO_SEQ",
        initialValue = 1,
        allocationSize = 1)
public class PaymentsInfo {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "PAYMENTS_INFO_SEQ_GENERATOR"
    )
    private Long paymentsNo;

    @Column(nullable = false, length = 100)
    private String payMethod;

    @Column(nullable = false, length = 100)
    private String impUid;

    @Column(nullable = false, length = 100)
    private String merchantUid;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false, length = 100)
    private String buyerAddr;

    @Column(nullable = false, length = 100)
    private String buyerPostcode;

    @OneToOne
    @JoinColumn(name = "work")
    private Work work;

    @ManyToOne
    @JoinColumn(name = "user")
    private User user;
}