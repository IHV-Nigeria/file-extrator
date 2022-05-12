package com.centradatabase.consumerapp.repository;

import com.centradatabase.consumerapp.model.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, Integer> {
    FileUpload findFileUploadByFileName(String fileName);
}
