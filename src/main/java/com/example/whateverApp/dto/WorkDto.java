package com.example.whateverApp.dto;

import com.example.whateverApp.model.entity.Work;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkDto {
    private Long id;
    @NotNull(message = "제목 ")
    private String title;
    @NotNull(message = "내용 ")
    private String context;
    @NotNull(message = "마감기한 ")
    private Integer deadLineTime;
    @NotNull(message = "보상금액 ")
    private Integer reward;
    private Double latitude;
    private Double longitude;
    private Double receiveLatitude;
    private Double receiveLongitude;
    private Long customerId;
    private Long helperId;
    private LocalDateTime createdTime;
    private LocalDateTime finishedAt;
    private int workProceedingStatus;


    public WorkDto(Work work){
        this.id = work.getId();
        this.title = work.getTitle();
        this.context = work.getContext();
        this.reward = work.getReward();
        this.latitude = work.getLatitude();
        this.longitude = work.getLongitude();
        this.receiveLatitude = work.getReceiveLatitude();
        this.receiveLongitude = work.getReceiveLongitude();
        this.customerId = work.getCustomer().getId();
        this.deadLineTime = work.getDeadLineTime();
        this.workProceedingStatus = work.getProceedingStatus().ordinal();
        this.createdTime = work.getCreatedTime();
        this.finishedAt = work.getFinishedAt();
        try {
            this.helperId = work.getHelper().getId();
            this.createdTime = work.getCreatedTime();
            this.finishedAt = work.getFinishedAt();
        }
        catch (NullPointerException e){

        }

    }
}
