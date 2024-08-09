package com.dsi.storage.core;

import java.io.InputStream;
import com.dsi.storage.azureblob.AzureBlobStorageService;
import com.dsi.storage.client.StorageClient;
import com.dsi.storage.googlecloud.GoogleCloudStorageService;
import com.dsi.storage.minio.MinioStorageService;
import com.dsi.storage.s3.S3StorageService;

public class StorageService {
    private final StorageClient storageClient;

    public StorageService() {
        String serviceType = System.getenv("STORAGE_SERVICE_TYPE");

        this.storageClient = switch (serviceType.toLowerCase()) {
            case "minio" -> new MinioStorageService();
            case "aws" -> new S3StorageService();
            case "azure" -> new AzureBlobStorageService();
            case "gcp" -> new GoogleCloudStorageService();
            default -> throw new IllegalStateException("Unsupported storage environment: " + serviceType);
        };
    }


    public String upload(String bucketName, String objectName, InputStream inputStream, String contentType) {
        return storageClient.upload(bucketName, objectName, inputStream, contentType);
    }

    public InputStream download(String bucketName, String objectName) {
        return storageClient.download(bucketName, objectName);
    }

    public InputStream download(String filePath){
        return storageClient.download(filePath);
    }
}
