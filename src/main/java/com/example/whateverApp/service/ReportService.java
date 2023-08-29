package com.example.whateverApp.service;

import com.example.whateverApp.dto.ReportDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.entity.Report;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.mongoRepository.ConversationRepository;
import com.example.whateverApp.repository.jpaRepository.ReportRepository;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.example.whateverApp.repository.jpaRepository.WorkRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final WorkRepository workRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ConversationRepository conversationRepository;

    public ReportDto createReport(ReportDto reportDto, HttpServletRequest request){
        Work work = workRepository.findById(reportDto.getWork().getId()).orElseThrow(() ->
                new CustomException(ErrorCode.WORK_NOT_FOUND));
        User user = jwtTokenProvider.getUser(request).orElseThrow(()->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Conversation conversation = conversationRepository.findByWorkId(reportDto.getWork().getId()).orElseThrow(() ->
                new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));


        Report report = Report.builder().reportReason(reportDto.getReportReason())
                .conversationId(conversation.get_id())
                .work(work)
                .user(user)
                .isFinished(false)
                .isReasonable(false)
                .build();

        reportRepository.save(report);
        return reportDto;
    }

    public List<Report> findNotFinishedReportList(){
        return reportRepository.findAll().stream().filter(report -> {
            return !report.isFinished();
        }).toList();
    }

    public Report get(Long reportId){
        return reportRepository.findById(reportId).orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
    }

}
