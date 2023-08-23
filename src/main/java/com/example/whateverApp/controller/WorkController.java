package com.example.whateverApp.controller;


import com.example.whateverApp.dto.WorkDto;
import com.example.whateverApp.model.document.Location;
import com.example.whateverApp.model.entity.Review;
import com.example.whateverApp.service.WorkServiceImpl;
import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class WorkController {

    private final WorkServiceImpl workService;

    @PostMapping("/work")
    public WorkDto createWork(@RequestBody WorkDto workDto, HttpServletRequest request) throws FirebaseMessagingException {
        return new WorkDto(workService.create(workDto, request));
    }

    /**
     *
     * 이거 상태코드 내려주는거 따로 공부해서 수정하기. 만약 일이 진행중이라면 상태코드 잘 내려주기 ㅇㅇ.
     */
    @PutMapping("/work/matching/{conversationId}")
    public WorkDto matchWork(@RequestBody WorkDto workDto, @PathVariable String conversationId, HttpServletRequest request){
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
    public WorkDto updateWork(@RequestBody WorkDto workDto) throws FirebaseMessagingException {
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
    public WorkDto finishWork(@PathVariable Long workId, HttpServletRequest request) {
        System.out.println("WorkController.finishWork");
        return workService.letFinish(workId, request);
    }

    @PutMapping("/work/success/{workId}")
    public WorkDto successWork(@PathVariable Long workId, @RequestBody Location location, HttpServletRequest request) {
        return workService.successWork(location, workId, request);
    }

    @PutMapping("/work/setRating/{workId}")
    public void setRating(@PathVariable Long workId, @RequestBody @Validated Review review, HttpServletRequest request){
        workService.setRating(workId, review, request);
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
