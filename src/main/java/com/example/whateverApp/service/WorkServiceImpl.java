package com.example.whateverApp.service;

import com.example.whateverApp.dto.MessageDto;
import com.example.whateverApp.dto.ReportDto;
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
import com.example.whateverApp.repository.jpaRepository.ReportRepository;
import com.example.whateverApp.repository.jpaRepository.UserRepository;
import com.example.whateverApp.repository.jpaRepository.WorkRepository;
import com.example.whateverApp.repository.mongoRepository.ConversationRepository;
import com.example.whateverApp.repository.mongoRepository.HelperLocationRepository;
import com.example.whateverApp.service.interfaces.WorkService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkServiceImpl implements WorkService {

    private final WorkRepository workRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final HelperLocationRepository helperLocationRepository;
    private final ConversationRepository conversationRepository;
    private final FirebaseCloudMessageService fcmService;
    private final UserServiceImpl userService;
    private final ReportService reportService;
    private final RewardService rewardService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private static final double EARTH_RADIUS = 6371;

    public Work create(WorkDto workDto, HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if(user.getAccountStatus().equals(AccountStatus.WILL_BAN))
            throw new CustomException(ErrorCode.WILL_BANNED_ACCOUNT);

        Work work = new Work().updateWork(workDto);
        work.setProceedingStatus(WorkProceedingStatus.CREATED);
        work.setCustomer(user);
        return workRepository.save(work);
    }

    @Override
    public WorkDto update(WorkDto workDto) {
        Work work = workRepository.findById(workDto.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
        work.updateWork(workDto);
        return new WorkDto(workRepository.save(work));
    }

    /**
     *
     * matchingHelper과 update함수를 구분해 놓은 이유는
     * update는 매번 호출 할 수 있는 함수지만
     * matchingHelper함수는 Helper와 Work가 매칭됐을 때 한번 실행된다.
     * 그리고 그 때 deadLineTime이 1시간이면 HelperLocation이 1분에 한번씩 저장되는 서비스가 제공된다.
     * HelperLocation이 1분에 한번씩 저장되는 서비스의 중복 호출을 막기 위해서 딱 한번만 실행하는 함수이다.
     */

    @Transactional
    public Work matchingHelper(WorkDto workDto, String conversationId, HttpServletRequest request) throws IOException {
        User requestUser = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Work work = workRepository.findById(workDto.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
        User helper = userRepository.findById(workDto.getHelperId()).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        User customer = userRepository.findById(work.getCustomer().getId()).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if(work.getProceedingStatus().equals(WorkProceedingStatus.STARTED))
            throw new CustomException(ErrorCode.ALREADY_PROCEED_WORK);

        if(work.getProceedingStatus().equals(WorkProceedingStatus.FINISHED))
            throw new CustomException(ErrorCode.ALREADY_FINISHED_WORK);

        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
        conversation.setWorkId(work.getId());
        work.setHelper(helper);
        work.setProceedingStatus(WorkProceedingStatus.STARTED);
        helper.setProceedingWork(true);
        customer.setProceedingWork(true);
        conversationRepository.save(conversation);


        if(work.getDeadLineTime() == 1){
            HelperLocation helperLocation = HelperLocation.builder().workId(work.getId()).locationList(new ArrayList<>()).build();
            helperLocationRepository.save(helperLocation);
        }

        if(requestUser.getId().equals(work.getHelper().getId())){ //심부름 요청서
            userService.setAvgReactTime(work, conversation);
        }

        fcmService.sendWorkProceeding(work, helper);
        userRepository.save(helper);
        userRepository.save(customer);
        return workRepository.save(work);
    }

    @Transactional
    public List<WorkDto> getWorkList(HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Work> workList = workRepository.findByCustomer(user);
        List<WorkDto> workDtos = new ArrayList<>();
        for (Work work : workList) {
            if(work.getProceedingStatus().equals(WorkProceedingStatus.CREATED))
                workDtos.add(new WorkDto(work));
        }
        return workDtos;
    }

    public List<WorkDto> getWorkListAll(HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Work> workList = workRepository.findByCustomer(user);
        workList.addAll(workRepository.findByHelper(user));
        List<WorkDto> workDtos = new ArrayList<>();
        for (Work work : workList) {
            workDtos.add(new WorkDto(work));
        }
        return workDtos;
    }

    @Override
    @Transactional
    public List<WorkDto> delete(Long workId, HttpServletRequest request) {
        User user = jwtTokenProvider.getUser(request)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));

        if(work.getCustomer().getId().equals(user.getId()))
            workRepository.deleteById(workId);

        else throw new CustomException(ErrorCode.BAD_REQUEST);

        return getWorkList(request);
    }

    @Override
    public WorkDto get(Long id, HttpServletRequest request) {

        return new WorkDto(workRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND)));
    }

    @Transactional
    public WorkDto successWork(Location location, Long workId, HttpServletRequest request) throws IOException {

        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));

        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if(!user.getId().equals(work.getHelper().getId()))
            throw new CustomException(ErrorCode.BAD_REQUEST);

        if(LocationServiceImpl.getDistance(location.getLatitude(), location.getLongitude(), work.getLatitude(), work.getLongitude()) > 500)
            throw new CustomException(ErrorCode.INVALID_LOCATION);

        work.setProceedingStatus(WorkProceedingStatus.FINISHED);
        fcmService.sendWorkProceeding(work, work.getCustomer());
        return new WorkDto(work);
    }

    @Override
    public WorkDto letFinish(Long workId, HttpServletRequest request) throws IOException {
        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Work work = workRepository.findById(workId).orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
        Conversation conversation = conversationRepository.findByWorkId(workId).orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
        conversation.setFinished(true);
        conversationRepository.save(conversation);

        if(user.getId().equals(work.getCustomer().getId())){
            work.setProceedingStatus(WorkProceedingStatus.REWARDED);
            work.setFinishedAt(LocalDateTime.now());
            rewardService.afterWork(work);
            fcmService.sendWorkProceeding(work, work.getHelper());
            fcmService.sendWorkProceeding(work, work.getCustomer());
            return new WorkDto(workRepository.save(work));
        }
        else {
            throw new CustomException(ErrorCode.BAD_REQUEST);}
    }

    /**
     * void letFinish(Work work)
     *
     * Scheduler Method
     * @param work
     * @throws IOException
     */
    @Transactional
    public void letFinish(Work work) throws IOException {
        Conversation conversation = conversationRepository.findByWorkId(work.getId()).orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
        conversation.setFinished(true);
        work.setProceedingStatus(WorkProceedingStatus.REWARDED);
        work.setFinishedAt(LocalDateTime.now());
        rewardService.afterWork(work);
        fcmService.sendWorkProceeding(work, work.getHelper());
        fcmService.sendWorkProceeding(work, work.getCustomer());
        simpMessagingTemplate.convertAndSend("/topic/chat/"+conversation.get_id(), new MessageDto("DeleteConv", conversation.get_id()));
    }

    public List<WorkDto> getWorkListByDistance(HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        double nowLatitude = user.getLatitude();
        double nowLongitude = user.getLongitude();
        double mForLatitude = (1 / (EARTH_RADIUS * 1 * (Math.PI / 180))) / 1000;
        double mForLongitude = (1 / (EARTH_RADIUS * 1 * (Math.PI / 180) * Math.cos(Math.toRadians(nowLatitude)))) / 1000;

        double maxY = nowLatitude + (5000 * mForLatitude);
        double minY = nowLatitude - (5000 * mForLatitude);
        double maxX = nowLongitude + (5000 * mForLongitude);
        double minX = nowLongitude - (5000 * mForLongitude);

        List<Work> nearByWorkList = workRepository.findAll().stream()
                .filter(u -> u.getLatitude().compareTo(maxY) <= 0)
                .filter(u -> u.getLatitude().compareTo(minY) >= 0)
                .filter(u -> u.getLongitude().compareTo(maxX) <= 0)
                .filter(u -> u.getLongitude().compareTo(minX) >= 0).toList();
        List<WorkDto> resultAroundWorkList = new ArrayList<>();
        WorkDto workDto;

        for (Work work : nearByWorkList) {
            if(work.getCreatedTime().plusHours(work.getDeadLineTime().longValue()).isAfter(LocalDateTime.now())) {
                if (work.getProceedingStatus().equals(WorkProceedingStatus.CREATED)) {
                    if(work.getCustomer().isAccountNonLocked() == true){
                     double distance = LocationServiceImpl.getDistance(nowLatitude, nowLongitude, work.getLatitude(), work.getLongitude());
                     if (distance < 5000) {
                         workDto = new WorkDto(work);
                            if (user.getId() != workDto.getCustomerId()) {
                             resultAroundWorkList.add(workDto);
                          }
                        }
                    }
                }
            }
        }

        return resultAroundWorkList;
    }

    public List<WorkDto> getWorkListByHelper(Long helperId) {
        User user = userRepository.findById(helperId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        List<Work> byHelper = workRepository.findByHelper(user);
        List<WorkDto> workDtoList = new ArrayList<>();
        for (Work work : byHelper) {
            workDtoList.add(new WorkDto(work));
        }
        return workDtoList;
    }

    public List<WorkDto> getWorkListByCustomer (Long customerId) {
        User user = userRepository.findById(customerId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        List<Work> byHelper = workRepository.findByCustomer(user);
        List<WorkDto> workDtoList = new ArrayList<>();
        for (Work work : byHelper) {
            workDtoList.add(new WorkDto(work));
        }
        return workDtoList;
    }

    public Work get(Long workId){
        return workRepository.findById(workId).orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
    }

    /**
     * 검증 해보기.
     */
    @Transactional
    public void executeUserAfterWork(Long workId) throws IOException {
        Work work = workRepository.findById(workId).orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));

        User customer = work.getCustomer();
        User helper = work.getHelper();
        List<Work> proceedingWorkList = workRepository.findByCustomerOrHelper(customer, customer).stream()
                .filter(w -> w.getProceedingStatus().equals(WorkProceedingStatus.STARTED)).toList();

        if(proceedingWorkList.size() == 0){
            customer.setProceedingWork(false);
            if(customer.getAccountStatus().equals(AccountStatus.WILL_BAN)) {
                reportService.executeReport(new ReportDto(customer.getPunishingDetail()), customer);
            }
        }

        proceedingWorkList = workRepository.findByCustomerOrHelper(helper, helper).stream()
                .filter(w -> w.getProceedingStatus().equals(WorkProceedingStatus.STARTED)).toList();

        if(proceedingWorkList.size() == 0){
            helper.setProceedingWork(false);
            if(helper.getAccountStatus().equals(AccountStatus.WILL_BAN)) {
                reportService.executeReport(new ReportDto(helper.getPunishingDetail()), helper);
            }
        }
    }
}
