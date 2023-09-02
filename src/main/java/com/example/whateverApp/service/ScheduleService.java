package com.example.whateverApp.service;

import com.example.whateverApp.model.AccountStatus;
import com.example.whateverApp.model.WorkProceedingStatus;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.jpaRepository.ReportRepository;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.example.whateverApp.repository.jpaRepository.WorkRepository;
import com.example.whateverApp.repository.mongoRepository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScheduleService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final WorkRepository workRepository;
    private final WorkServiceImpl workService;
    private final ConversationRepository conversationRepository;
    private final FirebaseCloudMessageService firebaseCloudMessageService;

    /**
     * 매 정각마다 정지된 유저들의 계정을 해제해주는 함수.
     */
    @Scheduled(cron = "10 0 * * *", zone = "Asia/Seoul")
    public void releaseBanAccounts(){
        LocalDateTime now = LocalDateTime.now();

        List<User> releaseUserList = userRepository.findAll().stream()
                .filter(u -> u.getAccountReleaseTime().isBefore(now)).toList();

        for (User user : releaseUserList) {
            user.setAccountReleaseTime(LocalDateTime.MIN);
            user.setAccountStatus(AccountStatus.USING);
        }
        userRepository.saveAll(releaseUserList);
    }

    @Scheduled(cron = "0 0 9 * *", zone = "Asia/Seoul")
    public void autoPermitNonFinishWorkList() {
        LocalDateTime now = LocalDateTime.now();
        List<Work> nonFinishWorkList = workRepository.findAll().stream()
                .filter(w -> w.getProceedingStatus().equals(WorkProceedingStatus.FINISHED))
                .filter(w -> w.getFinishedAt().plusDays(3).isBefore(now)).toList();

        try {
            for (Work work : nonFinishWorkList)
                workService.letFinish(work);
        } catch (Exception e){}

    }
}
