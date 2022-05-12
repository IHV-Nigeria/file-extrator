package com.centradatabase.consumerapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "filebatch")
public class FileBatch {
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO)
    private Integer filebatchId;
    private String filebatchStatus;
    private String zipFileName;
    private String batchNumber;
    private Date uploadDate;
    private String userId;
}
