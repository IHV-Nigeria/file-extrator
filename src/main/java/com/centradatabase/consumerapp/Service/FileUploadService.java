package com.centradatabase.consumerapp.Service;

import com.centradatabase.consumerapp.model.FileUpload;
import com.centradatabase.consumerapp.repository.FileUploadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileUploadService {

    @Autowired
    FileUploadRepository fileUploadRepository;


    public void updateFileUpload(FileUpload fileUpload){
         fileUploadRepository.save(fileUpload);
    }

    public void updateFileUploadList(List<FileUpload> fileUploadList){
        fileUploadRepository.saveAll(fileUploadList);
    }

    public FileUpload findFileUpload(String fileName){
       return fileUploadRepository.findFileUploadByFileName(fileName);
    }

    public List<FileUpload> findFileUploadList(String fileName){
        return fileUploadRepository.findFileUploadsByFileName(fileName);
    }

}
