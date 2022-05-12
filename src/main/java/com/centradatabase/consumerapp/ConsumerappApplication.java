package com.centradatabase.consumerapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

//@ComponentScan({"com.centradatabase.consumerapp.repository"})
public class ConsumerappApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerappApplication.class, args);
    }



}
