package com.example.whateverApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class FcmTest {
    private String operation;
    private String notification_key_name;
    private List<String> registration_ids = new ArrayList<>();
}
