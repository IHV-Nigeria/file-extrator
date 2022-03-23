package com.centradatabase.consumerapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class ConsumerappApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerappApplication.class, args);
    }



}
