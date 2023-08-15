package com.example.whateverApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableJpaRepositories(basePackages = {"com.example.whateverApp.repository.jpaRepository"})
@EnableMongoRepositories(basePackages = {"com.example.whateverApp.repository.mongoRepository"})
@SpringBootApplication
public class WhatEverAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(WhatEverAppApplication.class, args);
    }

}
