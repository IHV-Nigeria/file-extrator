package com.centradatabase.consumerapp.Service;

import com.centradatabase.consumerapp.model.FileBatch;
import com.centradatabase.consumerapp.model.Zipper;
import com.centradatabase.consumerapp.repository.FileBatchRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
public class FileExtractorService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    FileUploadService fileUploadService;

    @Autowired
    FileBatchRepository fileBatchRepository;

    Zipper zipper  = new Zipper();



    @Scheduled(fixedDelay = 2000L, initialDelay = 2000L)
    public  void job(){
     boolean containerList = zipper.unzip(rabbitTemplate,fileUploadService,fileBatchRepository);
//        FileBatch fileBatch = new FileBatch();
//        fileBatch.setFileBatchStatus("UPLOADED");
//        fileBatch.setUploadDate(new Date());
//        fileBatch.setBatchNumber("batch123");
//        fileBatch.setZipFileName("C:\\Users\\ihvn\\Documents\\MongoDB\\source\\file_3.zip");
//        fileBatch.setUserId("User 1");
//        fileBatchRepository.save(fileBatch);
       System.out.println("Testing Schedular");
    }
}
