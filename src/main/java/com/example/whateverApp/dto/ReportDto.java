package com.example.whateverApp.dto;

import com.example.whateverApp.model.ReportExecuteCode;
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
    String conversationId;
    Long workId;
    Long reportUserId;
    Long reportedUserId;
    String reportTitle;
    String reportReason;
    String executeDetail;
    int reportExecuteCode;

    public ReportDto(Report report){
        this.reportTitle = report.getReportTitle();
        this.id = report.getId();
        this.conversationId = report.getConversationId();
        this.reportUserId = report.getReportUser().getId();
        this.reportedUserId = report.getReportedUser().getId();
        this.reportReason = report.getReportReason();
        this.executeDetail = report.getExecuteDetail();
        this.reportExecuteCode = report.getReportExecuteCode().ordinal();
    }

}
