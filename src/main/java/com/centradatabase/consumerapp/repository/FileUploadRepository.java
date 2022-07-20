package com.centradatabase.consumerapp.repository;

import com.centradatabase.consumerapp.entities.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, Integer> {
    FileUpload findFileUploadByFileName(String fileName);
    List<FileUpload> findFileUploadsByFileName(String fileName);
}
