package com.example.whateverApp.dto;

import com.example.whateverApp.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.awt.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String name;
    private String introduce;
    private Double distance; //m
    private Double rating;
    private Long avgReactTime;
    private Double latitude;
    private Double longitude;
    private Long bankAccount;
    private String image;

    public UserDto(User user){
        id = user.getId();
        name = user.getName();
        introduce = user.getIntroduce();
        rating = user.getRating();
        avgReactTime = user.getAvgReactTime();
        latitude = user.getLatitude();
        bankAccount = user.getBankAccount();
        longitude = user.getLongitude();
    }
}
