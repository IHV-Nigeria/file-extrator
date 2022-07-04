package com.centradatabase.consumerapp.Service;

import com.centradatabase.consumerapp.model.Container;
import com.centradatabase.consumerapp.model.FileBatch;
import com.centradatabase.consumerapp.model.FileUpload;
import com.centradatabase.consumerapp.repository.FileBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZipperService {
    private final RabbitTemplate rabbitTemplate;
    private final FileUploadService fileUploadService;
    private final FileBatchRepository fileBatchRepository;

    @Value("${consumer.queue}")
    private String consumerQueue;

    @Value("${validator.queue}")
    private String validatorQueue;

    public String unzip() throws JAXBException {
        String UPLOAD_STATUS = "UPLOADED";
        List<FileBatch> fileBatchList = fileBatchRepository.findFileBatchByStatus(UPLOAD_STATUS);
        String uploadStatus;
        if(fileBatchList.size() > 0) {
            for (FileBatch fileBatch: fileBatchList) {
                fileBatch.setStatus("QUEUED");
                fileBatchRepository.save(fileBatch);
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(Container.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            for (FileBatch fileBatch : fileBatchList) {
                fileBatch.setStatus("PROCESSING");
                fileBatchRepository.save(fileBatch);
                String source = fileBatch.getZipFileName();
                File sourceFile = new File(source);

                try {
                    if (sourceFile.isFile() && sourceFile.exists()) {
                        String destination = sourceFile.getParent() + "\\destination";
                        extractFilesToProcess(sourceFile, destination);
                        File folder = new File(destination);
                        File[] listOfFiles = folder.listFiles();
                        if (listOfFiles != null && listOfFiles.length > 0) {
                            File[] listOfFile = getFilesInFolder(listOfFiles);
                            if (listOfFile != null)
                                listOfFiles = listOfFile;

                            log.info("listOfFiles Size: " + listOfFiles.length);

                            List<Container> containerList = new ArrayList<>();
                            List<File> fileList = new ArrayList<>();
                            convertXmlFilesToContainer(fileBatch, containerList, fileList, listOfFiles, jaxbUnmarshaller);
                            if (!containerList.isEmpty()) {
                                processContainersAndPushToQueue(fileBatch, containerList, fileList);
                            }
                            deleteFile(destination);
                            String EXTRACT_STATUS = "PROCESSED";
                            fileBatch.setStatus(EXTRACT_STATUS);
                            fileBatchRepository.save(fileBatch);
                            log.info(sourceFile.getName() + " extracted successfully");
                        } else
                            throw new NegativeArraySizeException("Folder is empty");
                    } else
                        throw new FileNotFoundException("Zipped file not found");
                } catch (JAXBException | NegativeArraySizeException | IOException e) {
                    log.error(e.getMessage());
                }
            }
            uploadStatus =  "Current files extracted successfully";
        } else {
            log.info("No current file to extract");
            uploadStatus = "No current file to extract";
        }
        return uploadStatus;
    }

    private void processContainersAndPushToQueue(FileBatch fileBatch, List<Container> containerList, List<File> fileList) {
        createFileUpload(containerList, fileBatch);
        rabbitTemplate.convertAndSend(validatorQueue, containerList);
        updateFileUpload(fileList);
        rabbitTemplate.convertAndSend(consumerQueue, containerList);
        containerList.clear();
        fileList.clear();
    }

    private void convertXmlFilesToContainer(FileBatch fileBatch, List<Container> containerList, List<File> fileList, File[] listOfFiles, Unmarshaller jaxbUnmarshaller) throws JAXBException {
        for (File file : listOfFiles) {
            if (file.isFile()) {
                try {
                    Container container = (Container) jaxbUnmarshaller.unmarshal(file);
                    container.setId(container.getMessageData().getDemographics().getPatientUuid());
                    containerList.add(container);
                    fileList.add(file);
                } catch (JAXBException | NullPointerException e) {
                    log.error("File is not in an acceptable format: {}", e.getMessage());
                    FileUpload fileUpload = FileUpload.builder()
                            .fileBatchId(fileBatch)
                            .status("FAILED")
                            .fileName(file.getName())
                            .uploadDate(fileBatch.getUploadDate())
                            .dataValidationReport("File not in acceptable format")
                            .build();
                    fileUploadService.updateFileUpload(fileUpload);
                }
                if (containerList.size() % 500 == 0 && containerList.size() != 0) {
                    processContainersAndPushToQueue(fileBatch, containerList, fileList);
                }
            }
        }
    }

    private File[] getFilesInFolder(File[] listOfFiles) {
        File[] listOfFile = null;
        for (File file : listOfFiles) {
            if (file.isDirectory()) {
                listOfFile = file.listFiles();
            }
        }
        return listOfFile;
    }

    private void extractFilesToProcess(File sourceFile, String destination) throws IOException {
        try (ZipFile zipFile = new ZipFile(sourceFile.getAbsolutePath())) {
            createDirectory(destination);
            zipFile.extractAll(destination);
        } catch (ZipException e) {
            e.printStackTrace();
            throw new ZipException("Destination folder not created");
        }
    }

    public static List<File> listf(String directoryName) {
        File directory = new File(directoryName);

        // get all the files from a directory
        File[] fList = directory.listFiles();
        assert fList != null;
        List<File> resultList = new ArrayList<>(Arrays.asList(fList));
        for (File file : fList) {
            if (file.isFile()) {
                resultList.add(file);
            } else if (file.isDirectory()) {
                resultList.addAll(listf(file.getAbsolutePath()));
            }
        }

        return resultList;
    }

    private static void deleteFile(String fileName){
        File file = new File(fileName);
        try{
            BasicFileAttributes basicFileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            if(basicFileAttributes.isRegularFile() && file.delete()){
                log.info("File Deleted");
            }

            if(basicFileAttributes.isDirectory()) {
                FileUtils.deleteDirectory(file);
                log.info("Directory Deleted");
            }
        }
        catch(Exception e){
            e.printStackTrace();
//            return false;
        }
//        return true;
    }

    private static void createDirectory(String filePath){
        try {
            File file = new File(filePath);
            if (!file.exists() && file.mkdir()) {
                log.info("Directory Created");
            } else
                log.error("Unable to create directory");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void createFileUpload(List<Container> containerList, FileBatch fileBatch) {
        List<FileUpload> fileUploadList = new ArrayList<>();
        for (Container container : containerList){
            List<FileUpload> fileUploadExist = fileUploadService.findFileUploadList(container.getMessageHeader().getFileName());
            if(fileUploadExist.size() == 0) {
                try {
                    FileUpload fileUpload = new FileUpload();
                    fileUpload.setFacilityDatimcode(container.getMessageHeader().getFacilityDatimCode());
                    fileUpload.setFileName(container.getMessageHeader().getFileName());
                    fileUpload.setFileTimestamp(new Timestamp(container.getMessageHeader().getTouchTime().getTime()));
                    fileUpload.setUploadDate(fileBatch.getUploadDate());
                    fileUpload.setStatus("UPLOADED");
                    UUID patientUuid = UUID.fromString(container.getMessageData().getDemographics().getPatientUuid());
                    fileUpload.setPatientUuid(patientUuid);
                    fileUpload.setFileBatchId(fileBatch);
                    fileUploadList.add(fileUpload);

                    //fileUploadService.updateFileUpload(fileUpload);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
        if(fileUploadList.size() > 0){
            fileUploadService.updateFileUploadList(fileUploadList);
            fileUploadList.clear();
            log.info("Upload Record saved");
        }

    }

    private  void updateFileUpload(List<File> currFileList){
        List<FileUpload> fileUploadList = new ArrayList<>();
        if(currFileList.size() > 0) {
            for(File currFile : currFileList) {
                try {
                    FileUpload fileUpload = fileUploadService.findFileUpload(currFile.getName());
                    if(fileUpload != null) {
                        fileUpload.setConsumerDate(new Date());
                        fileUpload.setStatus("VALIDATING");
                        fileUploadList.add(fileUpload);
                        //fileUploadService.updateFileUpload(fileUpload);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(fileUploadList.size() > 0){
                fileUploadService.updateFileUploadList(fileUploadList);
                fileUploadList.clear();
                log.info("Upload Record saved");
            }
        }
    }
}
