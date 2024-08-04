package com.example.fileuploadtester.test;

import com.dsi.storage.StorageServiceFactory;
import com.dsi.storage.core.StorageService;
import com.dsi.storage.minio.MinioStorageService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class FileStorageService {

    private final StorageService storageService;

    public FileStorageService() {
        try {
            storageService = StorageServiceFactory.createStorageService();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize storage service", e);
        }
    }

    public void upload(String bucketName, String objectName, InputStream data, String contentType) throws Exception {
        storageService.upload(bucketName, objectName, data, contentType);
    }

    public InputStream download(String bucketName, String objectName) throws Exception {
        return storageService.download(bucketName, objectName);
    }

    public void delete(String bucketName, String objectName) throws Exception {
        storageService.delete(bucketName, objectName);
    }
}