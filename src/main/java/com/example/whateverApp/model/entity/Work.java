package com.example.whateverApp.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@Data
@DynamicInsert
public class Work {

    @Id @GeneratedValue
    private Long id;
    private String title;
    private String context;
    private Integer deadLineTIme;
    /**
     * 23/03/11 chan
     *
     * deadLineTime의 단위 : 한시간
     * 만약 기한을 한시간으로 설정한 심부름은 다른 심부름에 비해 시간의 가치가 큰 경향이 있음.
     * 그러므로 한시간의 기한을 설정하면 자동으로 Seller의 위치가 5분단위로 기록됨.
     *
     * 기록된 위치정보는 신고서비스에 쓰임.
     */
    private Integer reward;
    private Float latitude;
    private Float longitude;
    @ColumnDefault("true")
    private boolean isProceeding; //진행중인 심부름 = true, 완료 = false

    @ManyToOne
    @JoinColumn(name = "customer_Id")
    private User customer;

    @ManyToOne
    @JoinColumn(name = "seller_Id")
    private User seller;

    @OneToOne
    private LocationConnection connection;

    @CreationTimestamp
    private LocalDateTime createdTime;
}
