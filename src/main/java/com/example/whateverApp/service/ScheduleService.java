package com.example.whateverApp.service;

import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.model.AccountStatus;
import com.example.whateverApp.model.WorkProceedingStatus;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.example.whateverApp.repository.jpaRepository.WorkRepository;
import com.example.whateverApp.repository.mongoRepository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final UserRepository userRepository;
    private final RewardService rewardService;
    private final WorkRepository workRepository;
    private final WorkServiceImpl workService;
    private final ConversationRepository conversationRepository;
    private final FirebaseCloudMessageService firebaseCloudMessageService;
    private final ConversationImpl conversationService;
    /**
     * 동일한 시간에 동일한 db의 동일 table을 조회한다고 했을 때
     * 동시성 문제가 생길 수 있음.
     * 그래서 10초씩 딜레이를 둬서 실행.
     */
    @Scheduled(cron = "10 0 * * * * ", zone = "Asia/Seoul")
    public void releaseBanAccounts(){
        LocalDateTime now = LocalDateTime.now();
        List<User> releaseUserList = userRepository.findAll().stream()
                .filter(u -> u.getAccountStatus().equals(AccountStatus.BAN))
                .filter(u -> now.isBefore(u.getAccountReleaseTime())).toList();
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
                .filter(w -> {
                    LocalDateTime finishedAt = w.getFinishedAt();
                    return finishedAt != null && finishedAt.plusDays(3).isBefore(now);
                })
                .filter(w -> w.getReportList().isEmpty())
                .toList();

        try {
            for (Work work : nonFinishWorkList) {
                String convId = workService.finishAuto(work);
                conversationService.sendDelete(convId);
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    @Scheduled(cron = "20 0 * * * *", zone = "Asia/Seoul")
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
        for (Conversation conversation : list)
            conversationService.sendDelete(conversation.get_id());
    }

    @Scheduled(cron = "30 0 * * * *", zone = "Asia/Seoul")
    public void deletePastWork() throws IOException {
        LocalDateTime now = LocalDateTime.now();
        List<Work> workList = workRepository.findAll().stream()
                .filter(w -> w.getProceedingStatus().equals(WorkProceedingStatus.CREATED))
                .filter(w -> w.getCreatedTime().plusHours(w.getDeadLineTime()).isBefore(now))
                .toList();

        for (Work work : workList) {
            rewardService.chargeToCustomer(work);
            firebaseCloudMessageService.workDeleteNotification(work);
            Optional<Conversation> conv = conversationRepository.findByWorkId(work.getId());
            if(conv.isPresent())
                conversationService.sendDelete(conv.get().get_id());
        }
        workRepository.deleteAll(workList);
    }

    @Scheduled(cron = "40 0 * * * *", zone = "Asia/Seoul")
    public void deleteNonFinishedWork() throws IOException {
        LocalDateTime now = LocalDateTime.now();
        List<Work> workList = workRepository.findAll().stream()
                .filter(w -> w.getProceedingStatus().equals(WorkProceedingStatus.STARTED))
                .filter(w -> w.getCreatedTime().plusDays(3).isBefore(now))
                .filter(w -> w.getReportList().size() == 0)
                .toList();

        for (Work work : workList) {
            rewardService.chargeToCustomer(work);
            firebaseCloudMessageService.notifyDeletedNonFinishWork(work);
            Conversation conversation = conversationRepository.findByWorkId(work.getId()).orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
            conversationService.sendDelete(conversation.get_id());
        }
        workRepository.deleteAll(workList);
    }
}
