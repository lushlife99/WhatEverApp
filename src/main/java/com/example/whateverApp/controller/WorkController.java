package com.example.whateverApp.controller;


import com.example.whateverApp.dto.WorkResponseDto;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.service.WorkServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class WorkController {

    private final WorkServiceImpl workService;

    @PostMapping("/work")
    public WorkResponseDto createWork(@RequestBody WorkResponseDto workDto, HttpServletRequest request){
        System.out.println("workDto = " + workDto.getLatitude());
        return new WorkResponseDto(workService.Create(workDto, request));
    }

    @PutMapping("/work")
    public WorkResponseDto updateWork(@RequestBody WorkResponseDto workResponseDto, HttpServletRequest request){
        return new WorkResponseDto(workService.update(workResponseDto));
    }


}
