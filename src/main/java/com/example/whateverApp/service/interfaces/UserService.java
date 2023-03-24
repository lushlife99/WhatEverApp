package com.example.whateverApp.service.interfaces;

import com.example.whateverApp.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService {

    User update(User user); //post update 둘다 이 함수 호출
    User delete(HttpServletRequest request);
    User get(HttpServletRequest request);
    User getPurchaseList(HttpServletRequest request);
    User getSellList(HttpServletRequest request);
    List<User> getByHighRating(HttpServletRequest request); // 거리안에 있는 사람들을 별점순으로 리턴
    List<User> getByCloseDistance(HttpServletRequest request); //거리 가까운 순서대로 리턴
}
