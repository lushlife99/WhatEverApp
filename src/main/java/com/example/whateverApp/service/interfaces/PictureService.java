package com.example.whateverApp.service.interfaces;

import jakarta.servlet.http.HttpServletRequest;

public interface PictureService {
    //리턴 타입은 나중에 지정하겠음. 아직 스프링에서 사진이 어떤타입으로 이루어져 있는지 모름
    void update(HttpServletRequest request);
    void get(HttpServletRequest request);
    void delete(HttpServletRequest request);
}
