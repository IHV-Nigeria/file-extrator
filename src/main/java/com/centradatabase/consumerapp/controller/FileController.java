package com.centradatabase.consumerapp.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

//@Controller
public class FileController {

    @Autowired
    RabbitTemplate rabbitTemplate;


    //ZipperService zipper  = new ZipperService();


//    @Scheduled(fixedDelay = 2000L, initialDelay = 2000L)
//    public  void job(){
//        boolean containerList = zipper.unzip(rabbitTemplate);
//        System.out.println("Testing Schedular");
//    }
}
