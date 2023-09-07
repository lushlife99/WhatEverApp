package com.example.whateverApp.service;

import com.example.whateverApp.dto.MessageDto;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final WorkRepository workRepository;
    private final WorkServiceImpl workService;
    private final ConversationRepository conversationRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     * 매 정각마다 정지된 유저들의 계정을 해제해주는 함수.
     */
    @Scheduled(cron = "1 0 * * * * ", zone = "Asia/Seoul")
    public void releaseBanAccounts(){
        LocalDateTime now = LocalDateTime.now();
        System.out.println("계정 해제 Scheduler 실행");
        List<User> releaseUserList = userRepository.findAll().stream()
                .filter(u -> u.getAccountStatus().equals(AccountStatus.BAN))
                .filter(u -> now.isBefore(u.getAccountReleaseTime())).toList();
        for (User user : releaseUserList) {
            System.out.println(user.getUserId());
        }
        for (User user : releaseUserList) {
            user.setAccountReleaseTime(null);
            user.setAccountStatus(AccountStatus.USING);
            user.setPunishingDetail(null);
        }
        userRepository.saveAll(releaseUserList);
    }

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
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

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void deleteDuplicatedConv() {
        LocalDateTime now = LocalDateTime.now();
        List<Conversation> list = conversationRepository.findAll();

        if (list != null) {
            list = list.stream()
                    .filter(c -> c.getWorkId() == 0L)
                    .filter(c -> c.getCreatedAt().plusDays(1).isBefore(now))
                    .toList();
        }
        conversationRepository.deleteAll(list);

        for (Conversation conversation : list) {
            System.out.println(conversation.get_id());
            simpMessagingTemplate.convertAndSend("/topic/chat/" + conversation.get_id(), new MessageDto("DeleteConv", conversation.get_id()));
        }

    }

}
