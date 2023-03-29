package com.example.whateverApp.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkResponseDto {
    private Long id;
    private String title;
    private String context;
    private Integer deadLineTIme;
    private Integer reward;
    private Float latitude;
    private Float longitude;
    private boolean isProceeding; //진행중인 심부름 = true, 완료 = false
    private Long customerId;
    private Long helperId;
}
