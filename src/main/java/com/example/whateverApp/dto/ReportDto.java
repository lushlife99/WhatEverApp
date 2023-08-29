package com.example.whateverApp.dto;

import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.entity.Report;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDto {

    Long id;
    Conversation conversation;
    WorkDto work;
    UserDto user;
    String reportReason;

    String executeDetail;
    boolean isReasonable;
    boolean isFinished;
    public ReportDto(Conversation conversation, Work work, User user){
        this.conversation = conversation;
        this.work = new WorkDto(work);
        this.user = new UserDto(user);
    }

}
