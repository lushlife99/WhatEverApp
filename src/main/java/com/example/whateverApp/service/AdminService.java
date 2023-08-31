package com.example.whateverApp.service;

import com.example.whateverApp.dto.*;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.AccountStatus;
import com.example.whateverApp.model.ReportExecuteCode;
import com.example.whateverApp.model.WorkProceedingStatus;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.entity.PaymentsInfo;
import com.example.whateverApp.model.entity.Report;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.jpaRepository.WorkRepository;
import com.example.whateverApp.repository.mongoRepository.ConversationRepository;
import com.example.whateverApp.repository.jpaRepository.ReportRepository;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.mongodb.CreateIndexCommitQuorum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ReportService reportService;
    private final AuthenticationManager authenticationManagerBuilder;
    private final ReportRepository reportRepository;
    private final ConversationRepository conversationRepository;
    private final WorkRepository workRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final PaymentService paymentService;

    public TokenInfo login(User user, HttpServletResponse response){

        User admin = userRepository.findByUserIdAndPassword(user.getUserId(),user.getPassword()).orElseThrow(()->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUserId(), user.getPassword());
        Authentication authentication = authenticationManagerBuilder.authenticate(authenticationToken);

        if(!authentication.getAuthorities().toString().contains("ROLE_ADMIN"))
            throw new CustomException(ErrorCode.UNAUTHORIZED_ADMIN);

        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication, response);
        tokenInfo.setId(admin.getId());
        return tokenInfo;
    }

    public Boolean adminCheck(HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if(!user.getRoles().contains("ROLE_ADMIN"))
            throw new CustomException(ErrorCode.UNAUTHORIZED_ADMIN);

        return true;
    }

    public ConversationDto getConv(String conversationId, HttpServletRequest request){
        adminCheck(request);

        return new ConversationDto(conversationRepository.findById(conversationId).orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND)));
    }

    public WorkDto getWork(Long workId, HttpServletRequest request){
        adminCheck(request);

        return new WorkDto(workRepository.findById(workId).orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND)));
    }

    /**
     * getUserInfo
     * 유저의 사진을 가져오는 로직 추가하기.
     *
     * @param userId
     * @param request
     * @return
     */

    public UserDto getUserInfo(Long userId, HttpServletRequest request){
        adminCheck(request);

        return new UserDto(userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)));
    }



    public List<ReportDto> getUserPunishList(Long userId, HttpServletRequest request){
       adminCheck(request);

        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        List<Report> list = reportRepository.findAll().stream()
                .filter(report -> report.getReportedUser().getId().equals(userId))
                .filter(report -> report.getReportExecuteCode().ordinal() > ReportExecuteCode.REJECT.ordinal()).toList();

        List<ReportDto> reportDtoList = new ArrayList<>();
        for (Report report : list)
            reportDtoList.add(new ReportDto(report));

        return reportDtoList;
    }

    public List<ReportDto> getReportListWriteByHelper(HttpServletRequest request){
        adminCheck(request);

        List<Report> list = findReportListWriteByHelper();

        List<ReportDto> reportDtoList = new ArrayList<>();
        for (Report report : list)
            reportDtoList.add(new ReportDto(report));

        return reportDtoList;
    }

    public List<ReportDto> getReportListWriteByCustomer(HttpServletRequest request){
        adminCheck(request);

        List<Report> list = findReportListWriteByCustomer();

        List<ReportDto> reportDtoList = new ArrayList<>();
        for (Report report : list)
            reportDtoList.add(new ReportDto(report));

        return reportDtoList;
    }

    public List<Report> findReportListWriteByHelper(){
        return reportRepository.findAll().stream().filter(report -> report.getReportExecuteCode().equals(ReportExecuteCode.BEFORE_EXECUTE))
                .filter(report -> report.getWork().getHelper().getId().equals(report.getReportUser().getId())).toList();
    }

    public List<Report> findReportListWriteByCustomer(){
        return reportRepository.findAll().stream().filter(report -> report.getReportExecuteCode().equals(ReportExecuteCode.BEFORE_EXECUTE))
                .filter(report -> report.getWork().getCustomer().getId().equals(report.getReportUser().getId())).toList();
    }

    public Report get(Long reportId){
        return reportRepository.findById(reportId).orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
    }

    public ReportDto executeReport(ReportDto reportDto, HttpServletRequest request){
        adminCheck(request);

        Report report = reportRepository.findById(reportDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
        Work work = workRepository.findById(reportDto.getWorkId()).orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
        User reportUser = userRepository.findById(reportDto.getReportUserId()).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        User reportedUser = userRepository.findById(reportDto.getReportedUserId()).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if(report.isExecuted())
            throw new CustomException(ErrorCode.BAD_REQUEST);

        switch (reportDto.getReportExecuteCode()){
            case 1 :
                break;
            case 2 :
                if(paymentService.refund(work))
                    reportUser.setReward(reportUser.getReward() + work.getReward());
                else throw new CustomException(ErrorCode.BAD_REQUEST);
                break;
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
        return new ReportDto((report));
    }

    public void banUserAccount(User user, int amountDayOfBan){
        user.setAccountStatus(AccountStatus.BAN);
        user.setAccountReleaseTime(LocalDateTime.now().plusDays(amountDayOfBan));
        simpMessagingTemplate.convertAndSend("/queue/"+user.getId(), new MessageDto("LogOut", new String("계정이 정지 당했습니다. 접속을 해제합니다.")));
        userRepository.save(user);
    }

    public void permanentBanUserAccount(User user){
        user.setAccountStatus(AccountStatus.PERMANENT_BAN);
        simpMessagingTemplate.convertAndSend("/queue/"+user.getId(), new MessageDto("LogOut", new String("계정이 정지 당했습니다. 접속을 해제합니다.")));
        userRepository.save(user);
    }

    public void joinAdmin(){
        User admin = User.builder()
                .userId("admin")
                .password("1234")
                .name("admin")
                .roles(Collections.singletonList("ROLE_ADMIN"))
                .imageFileName(UUID.randomUUID())
                .longitude(0.0)
                .latitude(0.0)
                .accountStatus(AccountStatus.USING)
                .build();
        Optional<User> byUserId = userRepository.findByUserId("admin");
        if(!byUserId.isPresent())
            userRepository.save(admin);
    }
}
