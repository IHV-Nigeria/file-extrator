package com.centradatabase.consumerapp.model;

import com.centradatabase.consumerapp.Service.FileUploadService;
import com.centradatabase.consumerapp.configs.rabbit.QueueNames;
import com.centradatabase.consumerapp.repository.FileBatchRepository;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.*;

@Slf4j
public class Zipper {


    private FileUploadService fileUploadService;
    private final String VALIDATINGSTATUS = "VALIDATING";
    private final String CONSUMESTATUS = "CONSUMED";
    private final String UPLOADSTATUS = "UPLOADED";
    private final String EXTRACTSTATUS = "EXTRACTED";

//    @Value("${consumed_file.folder}")
//    private String consumedFolder;
//
//    @Value("${zipped_file.folder}")
//    private String zippedFolder;

    public boolean unzip(RabbitTemplate rabbitTemplate, FileUploadService fileUploadService, FileBatchRepository fileBatchRepository) {


        this.fileUploadService = fileUploadService;

        List<FileBatch> fileBatchList= fileBatchRepository.findFileBatchByStatus(UPLOADSTATUS);
        if(fileBatchList.size() > 0)
            for(FileBatch fileBatch : fileBatchList) {
                String source = fileBatch.getZipFileName();
                File sourceFile = new File(source);
                String destination = sourceFile.getParent() + "\\destination";


//        File sourceDirectory = new File(source);


                List<Container> containerList = new ArrayList<>();
                List<File> fileList = new ArrayList<>();


                try {
//            if(sourceDirectory.isDirectory() && sourceDirectory.list().length > 0){
                    if (sourceFile.isFile() && sourceFile.exists()) {
                        //               File[] files = sourceDirectory.listFiles();
//                for (File currFile : files) {
                        try(ZipFile zipFile = new ZipFile(sourceFile.getAbsolutePath())) {
                            createDirectory(destination);
                            zipFile.extractAll(destination);
                        } catch (ZipException e) {
                            log.error(e.getMessage());
                        }


                        File folder = new File(destination);

                        File[] listOfFiles = folder.listFiles();

                        File[] listOfFile = null;
                        assert listOfFiles != null;
                        for (File file : listOfFiles) {
                            if (file.isDirectory()) {
                                listOfFile = file.listFiles();
                            }
                        }

                        JAXBContext jaxbContext = JAXBContext.newInstance(Container.class);
                        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

                        if (listOfFile != null)
                            listOfFiles = listOfFile;
                        log.info("listOfFiles Size: " + listOfFiles.length);
                        for (File file : listOfFiles) {
                            if (file.isFile()) {
                                Container container = (Container) jaxbUnmarshaller.unmarshal(file);


                                try {
                                    container.setId(container.getMessageData().getDemographics().getPatientUuid());
                                    containerList.add(container);
                                    fileList.add(file);
                                }catch (NullPointerException e){
                                    log.error(e.getMessage());
                                }


                                if (containerList.size() % 500 == 0 && containerList.size() != 0) {
                                    createFileUpload(containerList, fileBatch);
                                    rabbitTemplate.convertAndSend(QueueNames.VALIDATOR_QUEUE, containerList);
                                    updateFileUpload(fileList);
                                    rabbitTemplate.convertAndSend(QueueNames.CONSUMER_QUEUE, containerList);
                                    containerList.clear();
                                    fileList.clear();
                                }

                            }
                        }
                        if (!containerList.isEmpty()) {
                            createFileUpload(containerList, fileBatch);
                            rabbitTemplate.convertAndSend(QueueNames.VALIDATOR_QUEUE, containerList);
                            updateFileUpload(fileList);
                            rabbitTemplate.convertAndSend(QueueNames.CONSUMER_QUEUE, containerList);
                            containerList.clear();
                            fileList.clear();
                        }
                        deleteFile(destination);
                        // }
                    }

                } catch (Exception e) {

                    e.printStackTrace();
                }
//                Path path = Paths.get(sourceFile.getAbsolutePath());
//                Path destPath = Paths.get(consumedFolder + "\\" + source);
//                Files.move(path, destPath, StandardCopyOption.REPLACE_EXISTING);
//                if (sourceFile.delete()) log.info("File deleted");
                fileBatch.setStatus(EXTRACTSTATUS);
                fileBatchRepository.save(fileBatch);

            }
        return true;
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
            }
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
            }

            log.info("Upload Record saved");

        }

    }


}
