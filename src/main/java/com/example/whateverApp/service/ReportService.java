package com.example.whateverApp.service;

import com.example.whateverApp.dto.MessageDto;
import com.example.whateverApp.dto.ReportDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.AccountStatus;
import com.example.whateverApp.model.MessageType;
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
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private final String queuePrefix = "/queue/";

    public ReportDto createReport(ReportDto reportDto, HttpServletRequest request){
        Work work = workRepository.findById(reportDto.getWorkId()).orElseThrow(() ->
                new CustomException(ErrorCode.WORK_NOT_FOUND));
        User reportUser = jwtTokenProvider.getUser(request);
        Conversation conversation = conversationRepository.findByWorkId(reportDto.getWorkId()).orElseThrow(() ->
                new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
        User reportedUser;
        if(work.getCustomer().getId().equals(reportUser.getId()))
            reportedUser = work.getHelper();
        else reportedUser = work.getCustomer();
        if(reportRepository.findByWorkAndReportUser(work, reportUser).isPresent())
            throw new CustomException(ErrorCode.ALREADY_REPORT_THIS_WORK);
        if(work.getProceedingStatus().equals(WorkProceedingStatus.STARTED) || work.getProceedingStatus().equals(WorkProceedingStatus.CREATED) )
            throw new CustomException(ErrorCode.BAD_REQUEST);
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
        User user = jwtTokenProvider.getUser(request);
        if(!report.getReportUser().getId().equals(user.getId()))
            throw new CustomException(ErrorCode.BAD_REQUEST);

        reportRepository.deleteById(reportId);
    }

    @Transactional
    public void modifyReport(ReportDto reportDto, HttpServletRequest request){
        Report report = reportRepository.findById(reportDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
        User user = jwtTokenProvider.getUser(request);
        if(!report.getReportUser().getId().equals(user.getId()))
            throw new CustomException(ErrorCode.BAD_REQUEST);

        report.updateReport(reportDto);

        reportRepository.save(report);
    }

    public List<ReportDto> getMyReportList(HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request);
        List<ReportDto> reportDtoList = new ArrayList<>();
        for (Report report : user.getReportList())
            reportDtoList.add(new ReportDto(report));

        return reportDtoList;
    }
    @Transactional
    public ReportDto executeReport(ReportDto reportDto, User reportedUser) throws IOException {

        Report report = reportRepository.findById(reportDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
        Work work = workRepository.findById(reportDto.getWorkId()).orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
        Conversation conversation = conversationRepository.findByWorkId(work.getId()).orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
        if(report.isExecuted())
            throw new CustomException(ErrorCode.BAD_REQUEST);

        switch (reportDto.getReportExecuteCode()){
            case 3 :
                if(reportedUser.isProceedingWork()) {
                    reportedUser.setAccountStatus(AccountStatus.WILL_BAN);
                    reportedUser.setPunishingDetail(report);
                }
                else {
                    banUserAccount(reportedUser, 3);
                    report.setExecuted(true);
                }
                break;
            case 4 :
                if(reportedUser.isProceedingWork()) {
                    reportedUser.setAccountStatus(AccountStatus.WILL_BAN);
                    reportedUser.setPunishingDetail(report);
                }
                else {
                    banUserAccount(reportedUser, 7);
                    report.setExecuted(true);
                }
                break;
            case 5 :
                if(reportedUser.isProceedingWork()) {
                    reportedUser.setAccountStatus(AccountStatus.WILL_BAN);
                    reportedUser.setPunishingDetail(report);
                }
                else {
                    banUserAccount(reportedUser, 30);
                    report.setExecuted(true);
                }
                break;
            case 6 :
                if(reportedUser.isProceedingWork()) {
                    reportedUser.setAccountStatus(AccountStatus.WILL_BAN);
                    reportedUser.setPunishingDetail(report);
                }
                else {
                    permanentBanUserAccount(reportedUser);
                    report.setExecuted(true);
                }
                break;
            default:
                throw new CustomException(ErrorCode.BAD_REQUEST);
        }
        work.setProceedingStatus(WorkProceedingStatus.REWARDED);
        report.updateReport(reportDto);
        conversation.setFinished(true);
        conversationRepository.save(conversation);
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
        simpMessagingTemplate.convertAndSend(queuePrefix + user.getId(), new MessageDto(MessageType.LogOut.getDetail(), new String("계정이 정지 당했습니다. 접속을 해제합니다.")));
        userRepository.save(user);
    }

    public void permanentBanUserAccount(User user){
        user.setAccountStatus(AccountStatus.PERMANENT_BAN);
        simpMessagingTemplate.convertAndSend(queuePrefix + user.getId(), new MessageDto(MessageType.LogOut.getDetail(), new String("계정이 정지 당했습니다. 접속을 해제합니다.")));
        userRepository.save(user);
    }


    @Transactional
    public void executeAfterWork(Long workId) throws IOException {
        Work work = workRepository.findById(workId).orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
        List<Work> customerProceedingList = getProceedingWork(work.getCustomer());
        List<Work> helperProceedingList = getProceedingWork(work.getHelper());

        if(customerProceedingList.size() == 0)
            executeNonProceedingUser(work.getCustomer());

        if(helperProceedingList.size() == 0)
          executeNonProceedingUser(work.getHelper());
    }
    public void executeNonProceedingUser(User user) throws IOException {
        user.setProceedingWork(false);
        if(user.getAccountStatus().equals(AccountStatus.WILL_BAN))
            executeReport(new ReportDto(user.getPunishingDetail()), user);
    }
    public List<Work> getProceedingWork(User user){
        return workRepository.findByCustomerOrHelper(user, user).stream()
                .filter(w -> w.getProceedingStatus().equals(WorkProceedingStatus.STARTED)).toList();
    }


}
