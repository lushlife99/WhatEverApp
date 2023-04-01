package com.example.whateverApp.service;

import com.example.whateverApp.dto.WorkResponseDto;
import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.document.SellerLocation;
import com.example.whateverApp.model.entity.LocationConnection;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.*;
import com.example.whateverApp.service.interfaces.WorkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.Transient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkServiceImpl implements WorkService {

    private final WorkRepository workRepository;
    private final SellerLocationRepository sellerLocationRepository;
    private final LocationRepository locationRepository;
    private final LocationConnectionRepository locationConnectionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;


    public Work Create(WorkResponseDto workResponseDto, HttpServletRequest request) {
        // WorkResponseDto to Work
        Work work = new Work().updateWork(workResponseDto);
        LocationConnection locationConnection = new LocationConnection();
        Authentication authorization = jwtTokenProvider.getAuthentication(request.getHeader("Authorization").substring(7));
        User user = userRepository.findByUserId(authorization.getName()).get();
        work.setCustomer(user);
        if(workResponseDto.getDeadLineTime() == 1){
            createConnection(work);
        }
        return  workRepository.save(work);
    }

    @Transactional
    public LocationConnection createConnection(Work work){
        SellerLocation sellerLocation= new SellerLocation();
        String sellerLocationId = sellerLocationRepository.save(sellerLocation).getId();
        LocationConnection locationConnection = new LocationConnection();
        locationConnection.setSellerLocationId(sellerLocationId);
        work.setConnection(locationConnection);
        return locationConnectionRepository.save(locationConnection);
    }


    @Override
    public Work update(WorkResponseDto workResponseDto){
        Work work = workRepository.findById(workResponseDto.getId()).get();
        work.updateWork(workResponseDto);
        return workRepository.save(work);
    }

    @Override
    public Work delete(Long workId) {
        return null;
    }

    @Override
    public Work get(HttpServletRequest request) {
        return null;
    }

    @Override
    public Work success(Long workId) {
        return null;
    }
}
