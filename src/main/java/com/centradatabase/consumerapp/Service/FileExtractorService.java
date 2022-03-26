package com.centradatabase.consumerapp.Service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static com.centradatabase.consumerapp.model.Zipper.unzip;

@Service
public class FileExtractorService {

    @Autowired
    RabbitTemplate rabbitTemplate;


    @Scheduled(fixedDelay = 2000L, initialDelay = 2000L)
    public  void job(){
        boolean containerList = unzip(rabbitTemplate);
        System.out.println("Testing Schedular");
    }
}
