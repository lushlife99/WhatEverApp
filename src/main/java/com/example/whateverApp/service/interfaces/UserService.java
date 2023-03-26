package com.example.whateverApp.service.interfaces;

import com.example.whateverApp.dto.UserResponseDto;
import com.example.whateverApp.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService {

    UserResponseDto update(User user, HttpServletRequest request); //post update 둘다 이 함수 호출

    UserResponseDto getUserInfo(HttpServletRequest request);
    User delete(HttpServletRequest request);
    User get(HttpServletRequest request);
    User getPurchaseList(HttpServletRequest request);
    User getSellList(HttpServletRequest request);
    List<User> getByHighRating(HttpServletRequest request); // 거리안에 있는 사람들을 별점순으로 리턴
    List<User> getByCloseDistance(HttpServletRequest request); //거리 가까운 순서대로 리턴
}
