package com.example.whateverApp.service;

import com.example.whateverApp.dto.ReportDto;
import com.example.whateverApp.dto.TokenInfo;
import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.WorkProceedingStatus;
import com.example.whateverApp.model.entity.Report;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.mongoRepository.ConversationRepository;
import com.example.whateverApp.repository.jpaRepository.ReportRepository;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ReportRepository reportRepository;
    private final AuthenticationManager authenticationManagerBuilder;
    public List<ReportDto> getReportList(){
        List<Report> list = reportRepository.findAll().stream().filter(report -> {
            return !report.isFinished();
        }).toList();

        List<ReportDto> reportDtoList = new ArrayList<>();
        for (Report report : list) {
            reportDtoList.add(transformDto(report));
        }

        return reportDtoList;
    }

    public ReportDto executeReport(ReportDto reportDto){
        if(reportDto.isFinished())
            throw new CustomException(ErrorCode.ALREADY_EXECUTED_REPORT);

        Report report = reportRepository.findById(reportDto.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        if(reportDto.isReasonable()){
            report = report.updateReport(reportDto);
            Work work = report.getWork();
            User helper = work.getHelper();
            User customer = work.getCustomer();
            Integer reward = work.getReward();
            if(report.getUser().getId() == helper.getId())
                helper.setReward(helper.getReward() + reward);
            else
                customer.setReward(customer.getReward() + reward);

            userRepository.save(customer);
            userRepository.save(helper);
            /**
             * 돈 이동하게 만들기. ㅇㅇ
             */
            work.setProceedingStatus(WorkProceedingStatus.PAYED_REWORD);
        }

        report.setFinished(true);
        reportRepository.save(report);
        return transformDto(report);
    }

    public ReportDto transformDto(Report report){
        return ReportDto.builder()//.conversation(conversationRepository.findById(report.getConversationId()).get())
                        //.orElseThrow(()->new CustomException(ErrorCode.CONVERSATION_NOT_FOUND)))
                .work(new WorkDto(report.getWork()))
                .user(new UserDto(report.getUser()))
                .id(report.getId())
                .reportReason(report.getReportReason())
                .isReasonable(report.isReasonable())
                .isFinished(report.isFinished())
                .build();
    }

    public TokenInfo login(User user, HttpServletResponse response){
        User admin = userRepository.findByUserIdAndPassword(user.getUserId(),user.getPassword()).orElseThrow(()->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if(!admin.getRoles().contains("ROLE_ADMIN"))
            throw new CustomException(ErrorCode.UNAUTHORIZED_ADMIN);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUserId(), user.getPassword());

        Authentication authentication = authenticationManagerBuilder.authenticate(authenticationToken);
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication, response);
        tokenInfo.setId(admin.getId());
        return tokenInfo;

    }

}
