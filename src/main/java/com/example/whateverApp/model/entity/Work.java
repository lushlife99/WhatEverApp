package com.example.whateverApp.model.entity;

import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.model.WorkProceedingStatus;
import com.example.whateverApp.model.document.Location;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@DynamicInsert
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Work {

    @Id @GeneratedValue
    private Long id;
    private String title;
    private String context;
    private Integer deadLineTime = 0;
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
    private Double latitude = 0.1; //심부름 하는 Location
    private Double longitude = 0.1;
    private Double receiveLatitude = 0.0; //심부름 받는 Location
    private Double receiveLongitude = 0.0;

    @OneToOne
    private Review review;

    @ManyToOne
    @JoinColumn(name = "customer")
    private User customer;

    @ManyToOne
    @JoinColumn(name = "helper")
    private User helper;

    @OneToMany(mappedBy = "work")
    private List<Report> reportList;

    @Enumerated(EnumType.ORDINAL)
    private WorkProceedingStatus proceedingStatus;

    @CreationTimestamp
    private LocalDateTime createdTime;

    private LocalDateTime finishedAt;

    @OneToOne
    @JoinColumn(name = "paymentsInfoId")
    private PaymentsInfo paymentsInfo;


    public Work updateWork(WorkDto workDto){
        try {
            this.id = workDto.getId();
            this.title = workDto.getTitle();
            this.context = workDto.getContext();
            this.reward = workDto.getReward();
            if(workDto.getDeadLineTime().intValue() == 0){
                this.deadLineTime = 24;
            }
            else{
                this.deadLineTime = workDto.getDeadLineTime();
            }

            if(workDto.getLatitude().isNaN()){
                this.latitude = 0.0;
                this.longitude = 0.0;
            }
            else{
                this.latitude = workDto.getLatitude();
                this.longitude = workDto.getLongitude();
            }
            if(workDto.getReceiveLatitude().isNaN()){
                this.receiveLatitude = 0.0;
                this.receiveLongitude = 0.0;
            }
            else{
                this.receiveLatitude = workDto.getReceiveLatitude();
                this.receiveLongitude = workDto.getReceiveLongitude();
            }
        }catch (NullPointerException e){

        }
        return this;
    }
}
