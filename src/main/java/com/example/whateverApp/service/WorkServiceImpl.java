package com.example.whateverApp.service;


import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.error.CustomException;
import com.example.whateverApp.error.ErrorCode;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.document.Conversation;
import com.example.whateverApp.model.document.HelperLocation;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.entity.Review;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.jpaRepository.ReviewRepository;
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
    private final AlarmService alarmService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ConversationRepository conversationRepository;
    private final LocationServiceImpl locationService;
    private final ReviewRepository reviewRepository;
    private static final double EARTH_RADIUS = 6371;

    public WorkDto create(WorkDto workDto, HttpServletRequest request) throws IOException {
        Work work = new Work().updateWork(workDto);
        User user = jwtTokenProvider.getUser(request)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        work.setCustomer(user);
        Location location = new Location(workDto.getLatitude(), workDto.getLongitude());
        alarmService.sendNearByHelper(location, work);
        return new WorkDto(workRepository.save(work));
    }

    @Override
    public WorkDto update(WorkDto workDto){
        Work work = workRepository.findById(workDto.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
        work.updateWork(workDto);

        Location location = new Location(workDto.getLatitude(), workDto.getLongitude());
        //alarmService.sendNearByHelper(location);
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
    public Work matchingHelper(WorkDto workDto) {
        Work work = workRepository.findById(workDto.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));

        if(work.isProceeding())
            throw new CustomException(ErrorCode.ALREADY_PROCEED_WORK);

        if(work.isFinished())
            throw new CustomException(ErrorCode.ALREADY_FINISHED_WORK);

        User helper = userRepository.findById(workDto.getHelperId()).get();
        work.setHelper(helper);
        work.setProceeding(true);

        if(work.getDeadLineTime() == 1){
            HelperLocation helperLocation = HelperLocation.builder().workId(work.getId()).locationList(new ArrayList<>()).build();
            helperLocationRepository.save(helperLocation);
        }
        Conversation conversation = conversationRepository.findByWorkId(workDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));
        conversation.setWorkId(work.getId());
        return workRepository.save(work);
    }

    @Transactional
    public List<WorkDto> getWorkList(HttpServletRequest request){
        User user = jwtTokenProvider.getUser(request)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Work> workList = workRepository.findByCustomer(user);
        List<WorkDto> workDtos = new ArrayList<>();
        for (Work work : workList) {
            if(!work.isProceeding() && !work.isFinished() && work.getFinishedAt().equals(work.getCreatedTime()))
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
    public WorkDto successWork(Location location, Long workId, HttpServletRequest request) {

        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));

        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if(!user.getId().equals(work.getHelper().getId()))
            throw new CustomException(ErrorCode.BAD_REQUEST);

        if(LocationServiceImpl.getDistance(location.getLatitude(), location.getLongitude(), work.getLatitude(), work.getLongitude()) > 500)
            throw new CustomException(ErrorCode.INVALID_LOCATION);

        work.setProceeding(false);
        work.setFinishedAt(LocalDateTime.now());

        return new WorkDto(work);
    }

    @Override
    public WorkDto letFinish(Long workId, HttpServletRequest request) {
        User user = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Work work = workRepository.findById(workId).orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));

        if(user.getId().equals(work.getCustomer().getId())){
            work.setFinished(true);
            return new WorkDto(workRepository.save(work));
        }

        else throw new CustomException(ErrorCode.BAD_REQUEST);
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
                if (!work.isProceeding() || !work.isFinished()) {
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

        return resultAroundWorkList;
    }

    public void setRating(Long workId, Review review, HttpServletRequest request){
        User customer = jwtTokenProvider.getUser(request).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Work work = workRepository.findById(workId).orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
        if(reviewRepository.findByWork(work).isPresent())
            throw new CustomException(ErrorCode.DUPLICATE_REVIEW);

        if(!work.getCustomer().equals(customer))
            throw new CustomException(ErrorCode.BAD_REQUEST);

        review.setWork(work);
        review.setUser(work.getHelper());
        reviewRepository.save(review);
    }

}
