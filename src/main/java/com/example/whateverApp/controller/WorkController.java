package com.example.whateverApp.controller;


import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.service.WorkServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class WorkController {

    private final WorkServiceImpl workService;

    @PostMapping("/work")
    public Work createWork(Work work, HttpServletRequest request){
        return workService.Create(work, request);
    }

    @PutMapping("/work")
    public Work updateWork(Work work, HttpServletRequest request){
        return workService.update(work);
    }


}
