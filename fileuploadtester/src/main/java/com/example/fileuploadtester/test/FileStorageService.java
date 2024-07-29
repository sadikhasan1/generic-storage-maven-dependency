package com.example.fileuploadtester.test;

import com.dsi.storage.minio.MinioStorageService;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class FileStorageService {

    private final MinioStorageService storageService;

    public FileStorageService() {
        String endpoint = "https://play.min.io";
        String accessKey = "minioadmin";
        String secretKey = "minioadmin";
        String bucketName = "justjjjjjjjjjjjj";

        this.storageService = new MinioStorageService(endpoint, accessKey, secretKey, bucketName);
    }

    public void upload(String objectName, InputStream data, String contentType) throws Exception {
        storageService.upload(objectName, data, contentType);
    }

    public InputStream download(String objectName) throws Exception {
        return storageService.download(objectName);
    }

    public void delete(String objectName) throws Exception {
        storageService.delete(objectName);
    }
}