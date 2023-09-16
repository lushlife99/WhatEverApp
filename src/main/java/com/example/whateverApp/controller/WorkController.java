package com.example.whateverApp.controller;


import com.example.whateverApp.dto.ReviewDto;
import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.entity.Review;
import com.example.whateverApp.service.ReportService;
import com.example.whateverApp.service.WorkServiceImpl;
import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class WorkController {

    private final WorkServiceImpl workService;
    private final ReportService reportService;

    @PostMapping("/work")
    public WorkDto createWork(@RequestBody @Valid WorkDto workDto, HttpServletRequest request){
        return new WorkDto(workService.create(workDto, request));
    }

    @PutMapping("/work/deny/{conversationId}")
    public WorkDto denyWork(@RequestBody WorkDto workDto, @PathVariable String conversationId, HttpServletRequest request){
        return new WorkDto(workService.deny(workDto, conversationId, request));
    }

    @PutMapping("/work/matching/{conversationId}")
    public WorkDto matchWork(@RequestBody WorkDto workDto, @PathVariable String conversationId, HttpServletRequest request) throws IOException {
        return new WorkDto(workService.matchingHelper(workDto, conversationId, request));
    }

    @GetMapping("/work/{id}")
    public WorkDto getWork(@PathVariable Long id, HttpServletRequest request){
        return workService.get(id, request);
    }

    // 현재 끝나지 않은 심부름 리스트를 리턴
    @GetMapping("/workList")
    public List<WorkDto> getWorkList(HttpServletRequest request){
        return workService.getWorkList(request);
    }

    @PutMapping("/work")
    public WorkDto updateWork(@RequestBody WorkDto workDto)  {
        return workService.update(workDto);
    }

    // 모든 심부름 리스트를 리턴
    @GetMapping("/workList/all")
    public List<WorkDto> getWorkListAll(HttpServletRequest request){
        return workService.getWorkListAll(request);
    }

    @GetMapping("/workList/nearBy")
    public List<WorkDto> getWorkListByDistance(HttpServletRequest request) {
        return workService.getWorkListByDistance(request);
    }

    @DeleteMapping("/work/{workId}")
    public List<WorkDto> deleteWork(@PathVariable Long workId, HttpServletRequest request){
        return workService.delete(workId, request);
    }

    @PutMapping("/work/finish/{workId}")
    public WorkDto finishWork(@PathVariable Long workId, HttpServletRequest request) throws IOException {
        return workService.letFinish(workId, request);
    }

    @PutMapping("/work/success/{workId}")
    public WorkDto successWork(@PathVariable Long workId, @RequestBody Location location, HttpServletRequest request) throws IOException {
        WorkDto workDto = workService.successWork(location, workId, request);
        reportService.executeAfterWork(workId);
        return workDto;
    }

    @GetMapping("/workList/byHelper/{helperId}")
    public List<WorkDto> getWorkListByHelper(@PathVariable Long helperId){
        return workService.getWorkListByHelper(helperId);
    }

    @GetMapping("/workList/byCustomer/{customerId}")
    public List<WorkDto> getWorkListByCustomer(@PathVariable Long customerId) {
        return workService.getWorkListByCustomer(customerId);
    }
}
