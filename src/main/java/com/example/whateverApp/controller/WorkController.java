package com.example.whateverApp.controller;


import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.service.WorkServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class WorkController {

    private final WorkServiceImpl workService;

    @PostMapping("/work")
    public WorkDto createWork(@RequestBody WorkDto workDto, HttpServletRequest request){
        System.out.println("workDto = " + workDto.getLatitude());
        return new WorkDto(workService.Create(workDto, request));
    }

    @PutMapping("/work")
    public WorkDto updateWork(@RequestBody WorkDto workDto, HttpServletRequest request){
        return new WorkDto(workService.update(workDto));
    }


}
