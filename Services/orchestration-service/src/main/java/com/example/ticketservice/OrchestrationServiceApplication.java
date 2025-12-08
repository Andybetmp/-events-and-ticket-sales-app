package com.example.ticketservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRetry
@EnableScheduling
public class OrchestrationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrchestrationServiceApplication.class, args);
    }

}
