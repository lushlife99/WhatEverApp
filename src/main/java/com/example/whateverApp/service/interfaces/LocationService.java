package com.example.whateverApp.service.interfaces;

import com.example.whateverApp.dto.UserResponseDto;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.entity.Work;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

public interface LocationService {
    Page<UserResponseDto> findHelperByDistance(Pageable pageable, Location location, HttpServletRequest request);
    Page<UserResponseDto> findHelper(Pageable pageable, Location location, HttpServletRequest request);
    UserResponseDto setUserLocation(HttpServletRequest request, Location location);
    Location setSellerLocation(HttpServletRequest request, Work work);
    /**
     * setSellerLocation 함수.
     * deadLineTime이 1시간인 심부름은 seller의 위치를 저장함.
     * 10번 저장해야함. 5분마다 한번씩 서버 내부에서 재호출해야함.
     * 쓰레드 사용하면 될듯?
     */


}