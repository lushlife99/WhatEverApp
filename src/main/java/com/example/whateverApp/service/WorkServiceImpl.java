package com.example.whateverApp.service;

import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.AccountStatus;
import com.example.whateverApp.model.WorkProceedingStatus;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.document.HelperLocation;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.entity.Report;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.example.whateverApp.repository.jpaRepository.WorkRepository;
import com.example.whateverApp.repository.mongoRepository.ConversationRepository;
import com.example.whateverApp.repository.mongoRepository.HelperLocationRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkServiceImpl {

    private final WorkRepository workRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final HelperLocationRepository helperLocationRepository;
    private final ConversationRepository conversationRepository;
    private final FirebaseCloudMessageService fcmService;
    private final UserServiceImpl userService;
    private final RewardService rewardService;
    private final ConversationImpl conversationServiceImpl;
    private static final double EARTH_RADIUS = 6371;

    public WorkDto create(WorkDto workDto, HttpServletRequest request) {
        User user = jwtTokenProvider.getUser(request);
        if (user.getAccountStatus().equals(AccountStatus.WILL_BAN))
            throw new CustomException(ErrorCode.WILL_BANNED_ACCOUNT);

        Work work = new Work().updateWork(workDto);
        work.setProceedingStatus(WorkProceedingStatus.CREATED);
        work.setCustomer(user);
        rewardService.beforeWork(work, request);
        return new WorkDto(workRepository.save(work));
    }

    public WorkDto update(WorkDto workDto) {
        Work work = workRepository.findById(workDto.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
        work.updateWork(workDto);
        return new WorkDto(workRepository.save(work));
    }

    /**
     * matchingHelper과 update함수를 구분해 놓은 이유는
     * update는 매번 호출 할 수 있는 함수지만
     * matchingHelper함수는 Helper와 Work가 매칭됐을 때 한번 실행된다.
     * 그리고 그 때 deadLineTime이 1시간이면 HelperLocation이 1분에 한번씩 저장되는 서비스가 제공된다.
     * HelperLocation이 1분에 한번씩 저장되는 서비스의 중복 호출을 막기 위해서 딱 한번만 실행하는 함수이다.
     */

    @Transactional
    public WorkDto matchingHelper(WorkDto workDto, String conversationId, HttpServletRequest request) throws IOException {
        User requestUser = jwtTokenProvider.getUser(request);
        User helper = getUser(workDto.getHelperId());
        User customer = getUser(workDto.getCustomerId());
        Work work = getWork(workDto.getId());
        Conversation conversation = getConversation(conversationId);

        if (work.getProceedingStatus().equals(WorkProceedingStatus.STARTED))
            throw new CustomException(ErrorCode.ALREADY_PROCEED_WORK);
        if (work.getProceedingStatus().equals(WorkProceedingStatus.FINISHED))
            throw new CustomException(ErrorCode.ALREADY_FINISHED_WORK);

        conversation.setWorkId(work.getId());
        work.setHelper(helper);
        work.setProceedingStatus(WorkProceedingStatus.STARTED);
        helper.setProceedingWork(true);
        customer.setProceedingWork(true);
        if (work.getDeadLineTime() == 1) {
            HelperLocation helperLocation =
                    HelperLocation.builder()
                    .workId(work.getId())
                    .locationList(new ArrayList<>()).build();
            helperLocationRepository.save(helperLocation);
        }
        if (requestUser.getId().equals(work.getHelper().getId()))
            userService.setAvgReactTime(work, conversation);
        conversationRepository.save(conversation);
        fcmService.sendWorkProceeding(work, helper);
        return new WorkDto(work);
    }

    @Transactional
    public WorkDto deny(WorkDto workDto, String conversationId, HttpServletRequest request) {
        Work work = getWork(workDto.getId());
        User user = jwtTokenProvider.getUser(request);
        Conversation conversation = getConversation(conversationId);
        if (conversation.getCreatorId().equals(user.getId()) && conversation.getParticipantId().equals(user.getId()))
            throw new CustomException(ErrorCode.BAD_REQUEST);

        conversationServiceImpl.sendDelete(conversation.get_id());
        return new WorkDto(work);
    }

    @Transactional
    public List<WorkDto> getWorkList(HttpServletRequest request) {
        User user = jwtTokenProvider.getUser(request);

        List<Work> workList = workRepository.findByCustomer(user);
        List<WorkDto> workDtos = new ArrayList<>();
        for (Work work : workList) {
            if (work.getProceedingStatus().equals(WorkProceedingStatus.CREATED))
                workDtos.add(new WorkDto(work));
        }
        Collections.reverse(workDtos);
        return workDtos;
    }

    public List<WorkDto> getWorkListAll(HttpServletRequest request) {
        User user = jwtTokenProvider.getUser(request);
        List<Work> workList = workRepository.findByCustomer(user);

        workList.addAll(workRepository.findByHelper(user));
        List<WorkDto> workDtos = new ArrayList<>();
        for (Work work : workList) {
            workDtos.add(new WorkDto(work));
        }
        Collections.reverse(workDtos);
        return workDtos;
    }

    @Transactional
    public List<WorkDto> delete(Long workId, HttpServletRequest request) {
        User user = jwtTokenProvider.getUser(request);
        Work work = getWork(workId);

        if (work.getCustomer().getId().equals(user.getId()))
            workRepository.deleteById(workId);
        else throw new CustomException(ErrorCode.BAD_REQUEST);

        rewardService.chargeRewardToCustomer(work, request);
        return getWorkList(request);
    }

    public WorkDto get(Long id) {
        return new WorkDto(workRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND)));
    }

    @Transactional
    public WorkDto successWork(Location location, Long workId, HttpServletRequest request) throws IOException {
        Work work = getWork(workId);
        User user = jwtTokenProvider.getUser(request);

        if (!user.getId().equals(work.getHelper().getId()))
            throw new CustomException(ErrorCode.BAD_REQUEST);
        if (LocationServiceImpl.getDistance(location.getLatitude(), location.getLongitude(), work.getLatitude(), work.getLongitude()) > 500)
            throw new CustomException(ErrorCode.INVALID_LOCATION);

        work.setProceedingStatus(WorkProceedingStatus.FINISHED);
        fcmService.sendWorkProceeding(work, work.getCustomer());
        return new WorkDto(work);
    }

    @Transactional
    public WorkDto finish(Long workId, HttpServletRequest request) throws IOException {
        User user = jwtTokenProvider.getUser(request);
        Work work = getWork(workId);
        List<Report> reportList = work.getReportList().stream()
                .filter(r -> r.getReportUser().getId().equals(user.getId())).toList();
        Conversation conversation = getConversation(workId);

        if (!user.getId().equals(work.getCustomer().getId()))
            throw new CustomException(ErrorCode.BAD_REQUEST);
        if(reportList.size() != 0)
            throw new CustomException(ErrorCode.CANT_FINISH_REPORTED_WORK);

        conversation.setFinished(true);
        work.setProceedingStatus(WorkProceedingStatus.REWARDED);
        work.setFinishedAt(LocalDateTime.now());

        fcmService.sendWorkProceeding(work, work.getHelper());
        fcmService.sendWorkProceeding(work, work.getCustomer());
        conversationRepository.save(conversation);
        return new WorkDto(work);
    }

    @Transactional
    public String finishAuto(Work work) throws IOException {
        Conversation conversation = getConversation(work.getId());
        work.setProceedingStatus(WorkProceedingStatus.REWARDED);
        work.setFinishedAt(LocalDateTime.now());
        rewardService.addRewardToHelper(work.getId());
        fcmService.sendWorkProceeding(work, work.getHelper());
        fcmService.sendWorkProceeding(work, work.getCustomer());
        return conversation.get_id();
    }

    public List<WorkDto> getWorkListByDistance(HttpServletRequest request) {
        User user = jwtTokenProvider.getUser(request);
        List<Work> nearByWorkList = new ArrayList<>(getGetNearByWorkList(user));
        List<WorkDto> resultAroundWorkList = new ArrayList<>();
        nearByWorkList.removeIf(w -> w.getCustomer().getId().equals(user.getId()));

        for (Work work : nearByWorkList) {
            if (work.getCreatedTime().plusHours(work.getDeadLineTime().longValue()).isAfter(LocalDateTime.now()) && work.getProceedingStatus().equals(WorkProceedingStatus.CREATED)) {
                if (work.getCustomer().isAccountNonLocked()) {
                    double distance = LocationServiceImpl.getDistance(user.getLatitude(), user.getLongitude(), work.getLatitude(), work.getLongitude());
                    if (distance < 5000)
                        resultAroundWorkList.add(new WorkDto(work));
                }
            }
        }
        Collections.reverse(resultAroundWorkList);
        return resultAroundWorkList;
    }

    @NotNull
    private List<Work> getGetNearByWorkList(User user) {
        double nowLatitude = user.getLatitude();
        double nowLongitude = user.getLongitude();
        double mForLatitude = (1 / (EARTH_RADIUS * 1 * (Math.PI / 180))) / 1000;
        double mForLongitude = (1 / (EARTH_RADIUS * 1 * (Math.PI / 180) * Math.cos(Math.toRadians(nowLatitude)))) / 1000;
        double maxY = nowLatitude + (5000 * mForLatitude);
        double minY = nowLatitude - (5000 * mForLatitude);
        double maxX = nowLongitude + (5000 * mForLongitude);
        double minX = nowLongitude - (5000 * mForLongitude);

        return workRepository.findAll().stream()
                .filter(u -> u.getLatitude().compareTo(maxY) <= 0)
                .filter(u -> u.getLatitude().compareTo(minY) >= 0)
                .filter(u -> u.getLongitude().compareTo(maxX) <= 0)
                .filter(u -> u.getLongitude().compareTo(minX) >= 0).toList();
    }
    public List<WorkDto> getWorkListByHelper(Long helperId) {
        User user = getUser(helperId);
        List<Work> byHelper = workRepository.findByHelper(user);
        List<WorkDto> workDtoList = new ArrayList<>();

        for (Work work : byHelper)
            workDtoList.add(new WorkDto(work));

        Collections.reverse(workDtoList);
        return workDtoList;
    }

    public List<WorkDto> getWorkListByCustomer(Long customerId) {
        User user = getUser(customerId);
        List<Work> byHelper = workRepository.findByCustomer(user);
        List<WorkDto> workDtoList = new ArrayList<>();

        for (Work work : byHelper)
            workDtoList.add(new WorkDto(work));

        Collections.reverse(workDtoList);
        return workDtoList;
    }

    public User getUser(Long userId){
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public Work getWork(Long workId){
        return workRepository.findById(workId).orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
    }

    public Conversation getConversation(Long workId){
        return conversationRepository.findByWorkId(workId).orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
    }

    public Conversation getConversation(String conversationId){
        return conversationRepository.findById(conversationId).orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
    }
}
