package com.example.whateverApp.model.entity;

import com.example.whateverApp.dto.ReportDto;
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
    @JoinColumn(name = "user")
    private User user;

    @Lob
    private String reportReason;

    @Lob
    private String executeDetail;

    private boolean isReasonable = false;
    private boolean isFinished = false;

    @CreationTimestamp
    private LocalDateTime createdTime;

    public Report updateReport(ReportDto reportDto){
        this.reportReason = reportDto.getReportReason();
        this.executeDetail = reportDto.getExecuteDetail();
        this.isReasonable = reportDto.isReasonable();
        this.isFinished = reportDto.isFinished();

        return this;
    }

}
