package com.centradatabase.consumerapp.Service;

import com.centradatabase.consumerapp.model.FileBatch;
import com.centradatabase.consumerapp.model.Zipper;
import com.centradatabase.consumerapp.repository.FileBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;


@Service
@RequiredArgsConstructor
@Slf4j
public class FileExtractorService {
    private final Zipper zipper;



    @Scheduled(fixedDelay = 2000L, initialDelay = 2000L)
    public  void job() {
     String status = zipper.unzip();
//        FileBatch fileBatch = new FileBatch();
//        fileBatch.setFileBatchStatus("UPLOADED");
//        fileBatch.setUploadDate(new Date());
//        fileBatch.setBatchNumber("batch123");
//        fileBatch.setZipFileName("C:\\Users\\ihvn\\Documents\\MongoDB\\source\\file_3.zip");
//        fileBatch.setUserId("User 1");
//        fileBatchRepository.save(fileBatch);
       log.info(status);
    }
}
