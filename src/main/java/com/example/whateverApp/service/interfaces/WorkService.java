package com.example.whateverApp.service.interfaces;

import com.example.whateverApp.dto.WorkResponseDto;
import com.example.whateverApp.model.entity.Work;
import jakarta.servlet.http.HttpServletRequest;

public interface WorkService {

    Work  Create(WorkResponseDto workDto, HttpServletRequest request);
    Work update(WorkResponseDto workResponseDto);
    Work delete(Long workId);
    Work get(HttpServletRequest request);
    Work success(Long workId);
}
