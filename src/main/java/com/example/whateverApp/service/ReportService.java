package com.example.whateverApp.service;

import com.example.whateverApp.dto.MessageDto;
import com.example.whateverApp.dto.ReportDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.AccountStatus;
import com.example.whateverApp.model.ReportExecuteCode;
import com.example.whateverApp.model.WorkProceedingStatus;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.entity.Report;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.mongoRepository.ConversationRepository;
import com.example.whateverApp.repository.jpaRepository.ReportRepository;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.example.whateverApp.repository.jpaRepository.WorkRepository;
import com.google.firebase.database.core.Repo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final WorkRepository workRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final FirebaseCloudMessageService fcmService;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public ReportDto createReport(ReportDto reportDto, HttpServletRequest request){
        Work work = workRepository.findById(reportDto.getWorkId()).orElseThrow(() ->
                new CustomException(ErrorCode.WORK_NOT_FOUND));

        User reportUser = jwtTokenProvider.getUser(request).orElseThrow(()->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if(reportRepository.findByWorkAndReportUser(work, reportUser).isPresent())
            throw new CustomException(ErrorCode.ALREADY_REPORT_THIS_WORK);

        User reportedUser;

        if(work.getCustomer().getId().equals(reportUser.getId()))
            reportedUser = work.getHelper();
        else reportedUser = work.getCustomer();

        Conversation conversation = conversationRepository.findByWorkId(reportDto.getWorkId()).orElseThrow(() ->
                new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));


        Report report = Report.builder().reportReason(reportDto.getReportReason())
                .conversationId(conversation.get_id())
                .work(work)
                .reportTitle(reportDto.getReportTitle())
                .reportUser(reportUser)
                .reportedUser(reportedUser)
                .reportExecuteCode(ReportExecuteCode.BEFORE_EXECUTE)
                .build();
        reportRepository.save(report);
        return new ReportDto(report);
    }

    @Transactional
    public void deleteReport(Long reportId, HttpServletRequest request){
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if(!report.getReportUser().getId().equals(user.getId()))
            throw new CustomException(ErrorCode.BAD_REQUEST);

        reportRepository.deleteById(reportId);
    }

    @Transactional
    public void modifyReport(ReportDto reportDto, HttpServletRequest request){
        Report report = reportRepository.findById(reportDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if(!report.getReportUser().getId().equals(user.getId()))
            throw new CustomException(ErrorCode.BAD_REQUEST);

        report.updateReport(reportDto);

        reportRepository.save(report);
    }

    public List<ReportDto> getMyReportList(HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        List<ReportDto> reportDtoList = new ArrayList<>();
        for (Report report : user.getReportList())
            reportDtoList.add(new ReportDto(report));

        return reportDtoList;
    }
    public ReportDto executeReport(ReportDto reportDto, User reportedUser) throws IOException {

        Report report = reportRepository.findById(reportDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
        Work work = workRepository.findById(reportDto.getWorkId()).orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
        User reportUser = userRepository.findById(reportDto.getReportUserId()).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if(report.isExecuted())
            throw new CustomException(ErrorCode.BAD_REQUEST);

        switch (reportDto.getReportExecuteCode()){
            case 3 :
                if(reportedUser.isProceedingWork())
                    reportedUser.setAccountStatus(AccountStatus.WILL_BAN);
                else {
                    banUserAccount(reportedUser, 3);
                    report.setExecuted(true);
                }
                break;
            case 4 :
                if(reportedUser.isProceedingWork())
                    reportedUser.setAccountStatus(AccountStatus.WILL_BAN);
                else {
                    banUserAccount(reportedUser, 7);
                    report.setExecuted(true);
                }
                break;
            case 5 :
                if(reportedUser.isProceedingWork())
                    reportedUser.setAccountStatus(AccountStatus.WILL_BAN);
                else {
                    banUserAccount(reportedUser, 30);
                    report.setExecuted(true);
                }
                break;
            case 6 :
                if(reportedUser.isProceedingWork())
                    reportedUser.setAccountStatus(AccountStatus.WILL_BAN);
                else {
                    permanentBanUserAccount(reportedUser);
                    report.setExecuted(true);
                }
                break;
            default:
                throw new CustomException(ErrorCode.BAD_REQUEST);
        }
        report.updateReport(reportDto);
        reportRepository.save(report);
        fcmService.sendReportExecuted(report);
        return new ReportDto((report));
    }

    public void banUserAccount(User user, int amountDayOfBan){
        LocalDateTime localDateTime = LocalDateTime.now().plusDays(amountDayOfBan);
        if(localDateTime.getMinute() != 0){
            localDateTime.plusMinutes(60 - localDateTime.getMinute());
        }

        user.setAccountStatus(AccountStatus.BAN);
        user.setAccountReleaseTime(localDateTime);
        user.setAccountReleaseTime(LocalDateTime.now().plusDays(amountDayOfBan));
        simpMessagingTemplate.convertAndSend("/queue/"+user.getId(), new MessageDto("LogOut", new String("계정이 정지 당했습니다. 접속을 해제합니다.")));
        userRepository.save(user);
    }

    public void permanentBanUserAccount(User user){
        user.setAccountStatus(AccountStatus.PERMANENT_BAN);
        simpMessagingTemplate.convertAndSend("/queue/"+user.getId(), new MessageDto("LogOut", new String("계정이 정지 당했습니다. 접속을 해제합니다.")));
        userRepository.save(user);
    }




}
