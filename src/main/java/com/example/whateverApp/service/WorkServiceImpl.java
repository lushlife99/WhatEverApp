package com.example.whateverApp.service;

import com.example.whateverApp.jwt.JwtTokenProvider;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.document.SellerLocation;
import com.example.whateverApp.model.entity.LocationConnection;
import com.example.whateverApp.model.entity.User;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.repository.*;
import com.example.whateverApp.service.interfaces.WorkService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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


    public Work  Create(Work work, HttpServletRequest request) {
        SellerLocation sellerLocation;
        String sellerLocationId;
        LocationConnection locationConnection = new LocationConnection();
        if(work.getDeadLineTIme() == 1){
             sellerLocation= new SellerLocation();
             sellerLocationId = sellerLocationRepository.save(sellerLocation).getId();
            locationConnection.setSellerLocationId(sellerLocationId);
        }

        Authentication authorization = jwtTokenProvider.getAuthentication(request.getHeader("Authorization").substring(7));
        User user = userRepository.findByUserId(authorization.getName()).get();
        work.setCustomer(user);
        locationConnection.setWork(work);
        locationConnectionRepository.save(locationConnection);
        return workRepository.save(work);
    }

    @Override
    public Work update(Work work){
        Work findWork = workRepository.findById(work.getId()).get();

        return null;
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
