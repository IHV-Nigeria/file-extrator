package com.centradatabase.consumerapp.controller;

import com.centradatabase.consumerapp.model.Zipper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

//@Controller
public class FileController {

    @Autowired
    RabbitTemplate rabbitTemplate;


    //Zipper zipper  = new Zipper();


//    @Scheduled(fixedDelay = 2000L, initialDelay = 2000L)
//    public  void job(){
//        boolean containerList = zipper.unzip(rabbitTemplate);
//        System.out.println("Testing Schedular");
//    }
}
