package com.centradatabase.consumerapp.model;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Zipper {

    public static boolean unzip(RabbitTemplate rabbitTemplate){
        String source = "";
        String destination = "";
        File sourceDirectory = new File(source);

        List<Container> containerList = new ArrayList();

        try {
            if(sourceDirectory.isDirectory() && sourceDirectory.list().length > 0){
                File[] files = sourceDirectory.listFiles();
                for (File currFile : files) {
                    ZipFile zipFile = new ZipFile(currFile.getAbsolutePath());
                    createDirectory(destination);
                    zipFile.extractAll(destination);

                    deleteFile(currFile.getAbsolutePath());
                    File folder = new File(destination);

                    File[] listOfFiles = folder.listFiles();
                    System.out.println("listOfFiles size: " + listOfFiles);
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

                    for (File file : listOfFiles) {
                        if (file.isFile()) {
                            System.out.println(file.getName());
                            Container container = (Container) jaxbUnmarshaller.unmarshal(file);
                            container.setId(container.getMessageData().getDemographics().getPatientUuid());
                            containerList.add(container);

                        if(containerList.size() % 1000 ==0){
                            rabbitTemplate.convertAndSend("Queue-1",containerList);
                            containerList.clear();
                        }

                        }
                    }
                if(!containerList.isEmpty()){
                    rabbitTemplate.convertAndSend("Queue-1",containerList);
                    containerList.clear();
                }
                    deleteFile(destination);
            }
        }

        } catch (Exception e) {

            e.printStackTrace();
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
//                System.out.println(file.getAbsolutePath());
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


}
