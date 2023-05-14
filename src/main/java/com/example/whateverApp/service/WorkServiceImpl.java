package com.example.whateverApp.service;

import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.document.HelperLocation;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.*;
import com.example.whateverApp.service.interfaces.WorkService;
import com.google.api.Http;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkServiceImpl implements WorkService {

    private final WorkRepository workRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final HelperLocationRepository helperLocationRepository;


    public Work Create(WorkDto workDto, HttpServletRequest request) {
        // WorkResponseDto to Work
        Work work = new Work().updateWork(workDto);
        Authentication authorization = jwtTokenProvider.getAuthentication(request.getHeader("Authorization").substring(7));
        User user = userRepository.findByUserId(authorization.getName()).get();
        work.setCustomer(user);
        return  workRepository.save(work);
    }

    @Override
    public Work update(WorkDto workDto){
        Work work = workRepository.findById(workDto.getId()).get();
        work.updateWork(workDto);

        return workRepository.save(work);
    }

    /**
     * 23/04/02 chan
     *
     * matchingHelper과 update함수를 구분해 놓은 이유는
     * update는 매번 호출 할 수 있는 함수지만
     * matchingHelper함수는 Helper와 Work가 매칭됐을 때 한번 실행된다.
     * 그리고 그 때 deadLineTime이 1시간이면 HelperLocation이 1분에 한번씩 저장되는 서비스가 제공된다.
     * HelperLocation이 1분에 한번씩 저장되는 서비스의 중복 호출을 막기 위해서 딱 한번만 실행하는 함수이다.
     */

    public Work matchingHelper(WorkDto workDto) {
        Work work = workRepository.findById(workDto.getId()).get();
        if(!work.isProceeding()) {
            User helper = userRepository.findById(workDto.getHelperId()).get();
            work.setHelper(helper);
            work.setProceeding(true);
            if(work.getDeadLineTime() == 1){
                HelperLocation helperLocation = HelperLocation.builder().workId(work.getId()).locationList(new ArrayList<>()).build();
                helperLocationRepository.save(helperLocation);
            }
            return workRepository.save(work);
        }
        else{
            return null;
        }
    }

    public List<WorkDto> getWorkList(HttpServletRequest request){
        Authentication authentication = jwtTokenProvider.getAuthentication(request.getHeader("Authorization").substring(7));
        User user = userRepository.findByUserId(authentication.getName()).get();
        List<Work> workList = workRepository.findByCustomerId(user.getId());
        List<WorkDto> workDtos = new ArrayList<>();
        for (Work work : workList) {
            if(!work.isProceeding() && !work.isFinished())
            workDtos.add(new WorkDto(work));
        }
        return workDtos;
    }

    @Override
    public Work delete(Long workId) {
        return null;
    }

    @Override
    public Work get(Long id, HttpServletRequest request) {
        return workRepository.findById(id).get();
    }

    @Override
    public Work success(WorkDto workDto) {
        return null;
    }
}
