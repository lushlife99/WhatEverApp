package com.example.whateverApp.dto;

import com.example.whateverApp.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String name;
    private String introduce;
    private Double distance; //m
    private Double rating;
    private Integer avgReactTime;

    public UserResponseDto(User user){
        id = user.getId();
        name = user.getName();
        introduce = user.getIntroduce();
        rating = user.getRating();
        avgReactTime = user.getAvgReactTime();
    }

}
