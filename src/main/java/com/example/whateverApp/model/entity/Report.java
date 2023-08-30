package com.example.whateverApp.model.entity;

import com.example.whateverApp.dto.ReportDto;
import com.example.whateverApp.model.ReportExecuteCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String conversationId;

    @ManyToOne
    @JoinColumn(name = "work")
    private Work work;

    @ManyToOne
    @JoinColumn(name = "reportUser")
    private User reportUser;

    @ManyToOne
    @JoinColumn(name = "reportedUser")
    private User reportedUser;


    private String reportTitle;

    @Lob
    private String reportReason;

    @Lob
    private String executeDetail;

    @Enumerated(EnumType.ORDINAL)
    private ReportExecuteCode reportExecuteCode;

    private boolean executed = false;


    @CreationTimestamp
    private LocalDateTime createdTime;

    public Report updateReport(ReportDto reportDto){
        this.id = reportDto.getId();
        this.reportTitle = reportDto.getReportTitle();
        this.reportReason = reportDto.getReportReason();
        this.executeDetail = reportDto.getExecuteDetail();
        this.reportExecuteCode = ReportExecuteCode.values()[reportDto.getReportExecuteCode()];
        return this;
    }

}
