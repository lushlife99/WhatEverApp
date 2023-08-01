package com.example.whateverApp.service.interfaces;

import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.model.entity.Work;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;


public interface WorkService {

    Work  Create(WorkDto workDto, HttpServletRequest request);
    Work update(WorkDto workDto);
    List<WorkDto> delete(Long workId, HttpServletRequest request);
    Work get(Long id, HttpServletRequest request);
    Work letFinish(Long workId, HttpServletRequest request);
}
