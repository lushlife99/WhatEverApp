package com.example.whateverApp.service.interfaces;

import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.model.entity.Work;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;


public interface WorkService {

    Work create(WorkDto workDto, HttpServletRequest request) throws IOException, FirebaseMessagingException;
    WorkDto update(WorkDto workDto) throws IOException, FirebaseMessagingException;
    List<WorkDto> delete(Long workId, HttpServletRequest request);
    WorkDto get(Long id, HttpServletRequest request);
    WorkDto letFinish(Long workId, HttpServletRequest request);
}
