package com.centradatabase.consumerapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Fileupload")
public class FileUpload {
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO)
    private Integer fileuploadId;
    private String facilityDatimcode;
    private String fileName;
    private Timestamp fileTimestamp;
    private Date uploadDate;
    private Date consumerDate;
    private Date validatorDate;
    private Date deduplicationDate;
    private String schemaValidationReport;
    private String dataValidationReport;
    private Date etlDate;
    private String status;
    private String patientUuid;
   // private Integer filebatchId;




}
