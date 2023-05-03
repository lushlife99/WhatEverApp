package com.example.whateverApp.dto;


import com.example.whateverApp.model.entity.Work;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class WorkDto {
    private Long id;
    private String title;
    private String context;
    private Integer deadLineTime;
    private Integer reward;
    private Double latitude;
    private Double longitude;
    private boolean proceeding; //진행중인 심부름 = true, 완료 = false
    private Long customerId;
    private Long helperId;
    private boolean finished;

    public WorkDto(Work work){
        this.id = work.getId();
        this.title = work.getTitle();
        this.context = work.getContext();
        this.deadLineTime = work.getDeadLineTime();
        this.reward = work.getReward();
        this.latitude = work.getLatitude();
        this.longitude = work.getLongitude();
        this.proceeding = work.isProceeding();
        this.customerId = work.getCustomer().getId();
        try {
            this.helperId = work.getHelper().getId();
        }
        catch (Exception e){
        }
    }
}
