package com.centradatabase.consumerapp.Service;

import com.centradatabase.consumerapp.model.Zipper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;



@Service
public class FileExtractorService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    FileUploadService fileUploadService;

    Zipper zipper  = new Zipper();


    @Scheduled(fixedDelay = 2000L, initialDelay = 2000L)
    public  void job(){
        boolean containerList = zipper.unzip(rabbitTemplate,fileUploadService);
        System.out.println("Testing Schedular");
    }
}
