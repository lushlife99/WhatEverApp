package com.example.whateverApp.service.interfaces;

import com.example.whateverApp.dto.UserDto;
import com.example.whateverApp.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public interface UserService {

    UserDto update(UserDto userDto, HttpServletRequest request); //post update 둘다 이 함수 호출

    UserDto getUserInfo(HttpServletRequest request) throws MalformedURLException, IOException;
    void delete(HttpServletRequest request);
    User get(HttpServletRequest request);
    User getPurchaseList(HttpServletRequest request);
    User getSellList(HttpServletRequest request);
    List<User> getByHighRating(HttpServletRequest request); // 거리안에 있는 사람들을 별점순으로 리턴
    List<User> getByCloseDistance(HttpServletRequest request); //거리 가까운 순서대로 리턴
}
