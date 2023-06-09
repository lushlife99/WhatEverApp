package com.example.whateverApp.controller;


import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.model.entity.Work;
import com.example.whateverApp.service.WorkServiceImpl;
import com.example.whateverApp.service.interfaces.WorkService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class WorkController {

    private final WorkServiceImpl workService;

    @PostMapping("/work")
    public WorkDto createWork(@RequestBody WorkDto workDto, HttpServletRequest request){
        return new WorkDto(workService.Create(workDto, request));
    }

    /**
     *
     * 이거 상태코드 내려주는거 따로 공부해서 수정하기. 만약 일이 진행중이라면 상태코드 잘 내려주기 ㅇㅇ.
     */
    @PutMapping("/work/matching")
    public WorkDto matchWork(@RequestBody WorkDto workDto){
        return new WorkDto(workService.matchingHelper(workDto));
    }

    @GetMapping("/work/{id}")
    public WorkDto getWork(@PathVariable Long id, HttpServletRequest request){
        return new WorkDto(workService.get(id, request));
    }

    // 현재 끝나지 않은 심부름 리스트를 리턴
    @GetMapping("/workList")
    public List<WorkDto> getWorkList(HttpServletRequest request){
        return workService.getWorkList(request);
    }

    @PutMapping("/work")
    public WorkDto updateWork(@RequestBody WorkDto workDto){
        return new WorkDto(workService.update(workDto));
    }

    // 모든 심부름 리스트를 리턴
    @GetMapping("/workList/all")
    public List<WorkDto> getWorkListAll(HttpServletRequest request){
        return workService.getWorkListAll(request);
    }
}
