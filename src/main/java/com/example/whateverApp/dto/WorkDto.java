package com.example.whateverApp.dto;


import com.example.whateverApp.model.entity.Work;
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
    private String title;
    private String context;
    private Integer deadLineTime;
    private Integer reward;
    private Double latitude;
    private Double longitude;
    private Double receiveLatitude;
    private Double receiveLongitude;
    private boolean proceeding; //진행중인 심부름 = true, 완료 = false
    private Long customerId;
    private Long helperId;
    private boolean finished;
    private LocalDateTime createdTime;
    private LocalDateTime finishedAt;

    public WorkDto(Work work){
        this.id = work.getId();
        this.title = work.getTitle();
        this.context = work.getContext();
        this.reward = work.getReward();
        this.latitude = work.getLatitude();
        this.longitude = work.getLongitude();
        this.receiveLatitude = work.getReceiveLatitude();
        this.receiveLongitude = work.getReceiveLongitude();
        this.proceeding = work.isProceeding();
        this.customerId = work.getCustomer().getId();
        this.finished = work.isFinished();
        this.deadLineTime = work.getDeadLineTime();
        try {
            this.helperId = work.getHelper().getId();
            this.createdTime = work.getCreatedTime();
            this.finishedAt = work.getFinishedAt();
        }
        catch (NullPointerException e){

        }

    }
}
