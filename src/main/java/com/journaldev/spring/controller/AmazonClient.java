package com.journaldev.spring.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
@Service
public class AmazonClient {
	private AmazonS3Client s3client;

//    @Value("${amazonProperties.endpointUrl}")
//    private String endpointUrl;
//    @Value("${amazonProperties.bucketName}")
//    private String bucketName;
//    @Value("${amazonProperties.accessKey}")
//    private String accessKey;
//    @Value("${amazonProperties.secretKey}")
//    private String secretKey;
    
    private String endpointUrl = "https://s3.ap-south-1.amazonaws.com";
    private String bucketName = "bucket_name";
    private String accessKey = "user_access_key";
    private String secretKey = "user_secret_key";
    
    @PostConstruct
    private void initializeAmazon() {
       AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
       this.s3client = new AmazonS3Client(credentials);
    }
    
    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
    
    private String generateFileName(MultipartFile multiPart) {
        return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
    }
    
    private void uploadFileTos3bucket(String fileName, File file) {
        s3client.putObject(new PutObjectRequest(bucketName, fileName, file));
    }
    
    public String uploadFile(MultipartFile multipartFile) {

        String fileUrl = "";
        String fileName = "";
        try {
            File file = convertMultiPartToFile(multipartFile);
            fileName = generateFileName(multipartFile);
            fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
            uploadFileTos3bucket(fileName, file);
            file.delete();
        } catch (Exception e) {
           e.printStackTrace();
        }
        return fileName;
//    	String filenameExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
//		
//		String key = UUID.randomUUID().toString() + "." +filenameExtension;
//		
//		ObjectMetadata metaData = new ObjectMetadata();
//		metaData.setContentLength(file.getSize());
//		metaData.setContentType(file.getContentType());
//		
//		try {
//			s3client.putObject("my-videos-bucket-05", key, file.getInputStream(), metaData);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		s3client.setObjectAcl("my-videos-bucket-05", key, CannedAccessControlList.PublicRead);
//		
//		return s3client.getResourceUrl("my-videos-bucket-05", key);
    }
    
    public String deleteFileFromS3Bucket(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        s3client.deleteObject(new DeleteObjectRequest(bucketName + "/", fileName));
        return "Successfully deleted";
    }
}
