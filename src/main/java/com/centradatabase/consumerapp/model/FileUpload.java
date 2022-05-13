package com.centradatabase.consumerapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "file_upload")
public class FileUpload {
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO)
    private Integer fileUploadId;
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
    private UUID patientUuid;
    @OneToOne
    @JoinColumn(name = "file_batch_id",referencedColumnName = "fileBatchId")
    private FileBatch fileBatchId;




}
