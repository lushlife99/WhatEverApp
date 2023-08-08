package com.example.whateverApp.service.interfaces;

import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.model.entity.Work;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;


public interface WorkService {

    WorkDto  create(WorkDto workDto, HttpServletRequest request);
    WorkDto update(WorkDto workDto);
    List<WorkDto> delete(Long workId, HttpServletRequest request);
    WorkDto get(Long id, HttpServletRequest request);
    WorkDto letFinish(Long workId, HttpServletRequest request);
}
