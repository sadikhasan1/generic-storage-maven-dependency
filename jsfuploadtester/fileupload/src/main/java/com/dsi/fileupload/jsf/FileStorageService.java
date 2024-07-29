package com.dsi.fileupload.jsf;

import com.dsi.storage.minio.MinioStorageService;

import java.io.InputStream;

public class FileStorageService {

    private final MinioStorageService storageService;

    public FileStorageService() {
        String endpoint = "http://192.168.122.224:9000";
        String accessKey = "minioadmin";
        String secretKey = "minioadmin";
        String bucketName = "scratchbucket";

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