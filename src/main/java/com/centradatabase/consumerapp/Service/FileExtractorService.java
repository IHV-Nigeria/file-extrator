package com.centradatabase.consumerapp.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;


@Service
@RequiredArgsConstructor
@Slf4j
public class FileExtractorService {
    private final ZipperService zipperService;

    @Scheduled(fixedDelay = 2000L, initialDelay = 2000L)
    public  void job() throws JAXBException {
     String status = zipperService.unzip();
     log.info(status);
    }
}
