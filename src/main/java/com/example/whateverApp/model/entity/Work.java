package com.example.whateverApp.model.entity;

import com.example.whateverApp.dto.WorkDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private Integer deadLineTime;
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
    private Double latitude;
    private Double longitude;
    @ColumnDefault("true")
    private boolean proceeding; //진행중인 심부름 = true, 완료 = false
    @ColumnDefault("true")
    private boolean finished;

    @ManyToOne
    @JoinColumn(name = "customer_Id")
    private User customer;

    @ManyToOne
    @JoinColumn(name = "helper_Id")
    private User helper;

    @JsonIgnore
    @OneToOne
    private LocationConnection connection;

    @CreationTimestamp
    private LocalDateTime createdTime;

    public Work updateWork(WorkDto workDto){
        try {
            this.id = workDto.getId();
            this.title = workDto.getTitle();
            this.context = workDto.getContext();
            this.deadLineTime = workDto.getDeadLineTime();
            this.latitude = workDto.getLatitude();
            this.longitude = workDto.getLongitude();
            this.proceeding = workDto.isProceeding();
        }catch (NullPointerException e){

        }
        return this;
    }
}
