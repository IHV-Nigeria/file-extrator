package com.centradatabase.consumerapp.repository;

import com.centradatabase.consumerapp.model.FileBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileBatchRepository extends JpaRepository<FileBatch, Integer> {
    FileBatch findFileBatchByZipFileName(String name);
    List<FileBatch> findFileBatchByStatus(String status);
}
