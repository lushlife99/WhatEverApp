package com.example.whateverApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.TimeZone;

@EnableJpaRepositories(basePackages = {"com.example.whateverApp.repository.jpaRepository"})
@EnableMongoRepositories(basePackages = {"com.example.whateverApp.repository.mongoRepository"})
@EnableScheduling
@SpringBootApplication
public class WhatEverAppApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        SpringApplication.run(WhatEverAppApplication.class, args);
    }

}
