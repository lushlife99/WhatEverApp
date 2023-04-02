package com.example.whateverApp.service.interfaces;

import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.model.entity.Work;
import jakarta.servlet.http.HttpServletRequest;

public interface WorkService {

    Work  Create(WorkDto workDto, HttpServletRequest request);
    Work update(WorkDto workDto);
    Work delete(Long workId);
    Work get(HttpServletRequest request);
    Work success(WorkDto workDto);
}
