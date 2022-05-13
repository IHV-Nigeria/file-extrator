package com.centradatabase.consumerapp.model;

import com.centradatabase.consumerapp.Service.FileUploadService;
import com.centradatabase.consumerapp.configs.rabbit.QueueNames;
import com.centradatabase.consumerapp.repository.FileBatchRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.scheduling.annotation.Scheduled;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class Zipper {


    private RabbitTemplate rabbitTemplate;
    private FileUploadService fileUploadService;
    private FileBatchRepository fileBatchRepository;
    private final String VALIDATINGSTATUS = "VALIDATING";
    private final String CONSUMESTATUS = "CONSUMED";
    private final String UPLOADSTATUS = "UPLOADED";
    private final String EXTRACTSTATUS = "EXTRACTED";

    public   boolean unzip(RabbitTemplate rabbitTemplate, FileUploadService fileUploadService, FileBatchRepository fileBatchRepository){


        this.rabbitTemplate = rabbitTemplate;
        this.fileUploadService = fileUploadService;
        this.fileBatchRepository = fileBatchRepository;

        List<FileBatch> fileBatchList= fileBatchRepository.findFileBatchByFilebatchStatus(UPLOADSTATUS);
        if(fileBatchList.size() > 0)
            for(FileBatch fileBatch : fileBatchList) {
                String source = fileBatch.getZipFileName();
                File sourceFile = new File(source);
                String destination = sourceFile.getParent() + "\\destination";


//        File sourceDirectory = new File(source);


                List<Container> containerList = new ArrayList();
                List<File> fileList = new ArrayList();


                try {
//            if(sourceDirectory.isDirectory() && sourceDirectory.list().length > 0){
                    if (sourceFile.isFile() && sourceFile.exists()) {
                        //               File[] files = sourceDirectory.listFiles();
//                for (File currFile : files) {

                        ZipFile zipFile = new ZipFile(sourceFile.getAbsolutePath());
                        createDirectory(destination);
                        zipFile.extractAll(destination);


                        File folder = new File(destination);

                        File[] listOfFiles = folder.listFiles();

                        File[] listOfFile = null;
                        for (File file : listOfFiles) {
                            if (file.isDirectory()) {
                                listOfFile = file.listFiles();
                            }
                        }

                        JAXBContext jaxbContext = JAXBContext.newInstance(Container.class);
                        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

                        if (listOfFile != null)
                            listOfFiles = listOfFile;
                        System.out.println("listOfFiles Size: " + listOfFiles.length);
                        for (File file : listOfFiles) {
                            if (file.isFile()) {
                                System.out.println(file.getName());
                                Container container = (Container) jaxbUnmarshaller.unmarshal(file);
                                
                                container.setId(container.getMessageData().getDemographics().getPatientUuid());
                                containerList.add(container);
                                fileList.add(file);


                                if (containerList.size() % 500 == 0) {
                                    createFileUpload(containerList,UPLOADSTATUS,fileBatch);
                                    rabbitTemplate.convertAndSend(QueueNames.VALIDATOR_QUEUE, containerList);
                                    updateFileUpload(fileList, VALIDATINGSTATUS);
                                    rabbitTemplate.convertAndSend(QueueNames.CONSUMER_QUEUE, containerList);
                                    containerList.clear();
                                    fileList.clear();
                                }

                            }
                        }
                        if (!containerList.isEmpty()) {
                            createFileUpload(containerList,UPLOADSTATUS,fileBatch);
                            rabbitTemplate.convertAndSend(QueueNames.VALIDATOR_QUEUE, containerList);
                            updateFileUpload(fileList, VALIDATINGSTATUS);
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
                fileBatch.setFilebatchStatus(EXTRACTSTATUS);
                fileBatchRepository.save(fileBatch);

            }
        return true;
    }

    public static List<File> listf(String directoryName) {
        File directory = new File(directoryName);

        List<File> resultList = new ArrayList<>();

        // get all the files from a directory
        File[] fList = directory.listFiles();
        resultList.addAll(Arrays.asList(fList));
        for (File file : fList) {
            if (file.isFile()) {
                resultList.add(file);
            } else if (file.isDirectory()) {
                resultList.addAll(listf(file.getAbsolutePath()));
            }
        }
        System.out.println("Size: "+resultList.size());

        return resultList;
    }

    private static boolean deleteFile(String fileName){
        File file = new File(fileName);
        try{
            if(file.isFile()){
                file.delete();
                System.out.println("File Deleted");
            }

            if(file.isDirectory()) {
                FileUtils.deleteDirectory(file);
                System.out.println("Directory Deleted");
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void createDirectory(String filePath){
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
                System.out.println("Directory Created");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void createFileUpload(List<Container> containerList, String status, FileBatch fileBatch) {
        List<FileUpload> fileUploadList = new ArrayList<>();
        for (Container container : containerList){
            List<FileUpload> fileUploadExist = fileUploadService.findFileUploadList(container.getMessageHeader().getFileName());
            if(fileUploadExist.size() == 0) {
                try {
                    FileUpload fileUpload = new FileUpload();
                    fileUpload.setFacilityDatimcode(container.getMessageHeader().getFacilityDatimCode());
                    fileUpload.setFileName(container.getMessageHeader().getFileName());
                    fileUpload.setFileTimestamp(new Timestamp(container.getMessageHeader().getTouchTime().getTime()));
                    fileUpload.setUploadDate(fileBatchRepository.findById(fileBatch.getFilebatchId()).get().getUploadDate());
                    fileUpload.setStatus(status);
                    fileUpload.setPatientUuid(container.getMessageData().getDemographics().getPatientUuid());
                    fileUpload.setFilebatchId(fileBatchRepository.findById(fileBatch.getFilebatchId()).get());
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
            System.out.println("Upload Record saved");
        }

    }

    private  void updateFileUpload(List<File> currFileList,String status){
        List<FileUpload> fileUploadList = new ArrayList<>();
        if(currFileList.size() > 0) {
            for(File currFile : currFileList) {
                try {
                    FileUpload fileUpload = fileUploadService.findFileUpload(currFile.getName());
                    if(fileUpload != null) {
                        fileUpload.setConsumerDate(new Date());
                        fileUpload.setStatus(status);
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

            System.out.println("Upload Record saved");

        }

    }


}
