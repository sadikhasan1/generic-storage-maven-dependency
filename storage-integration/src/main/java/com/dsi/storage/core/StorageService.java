package com.dsi.storage.core;

import java.io.InputStream;

import com.dsi.storage.azureblob.AzureBlobStorageService;
import com.dsi.storage.dto.BucketObject;
import com.dsi.storage.googlecloud.GoogleCloudStorageService;
import com.dsi.storage.minio.MinioStorageService;
import com.dsi.storage.s3.S3StorageService;
import com.dsi.storage.util.FileUtils;
import io.minio.*;

public class StorageService {
    public static String upload(String bucketName, String objectName, InputStream inputStream, String contentType) {
        String serviceType = System.getenv("STORAGE_SERVICE_TYPE");

        return switch (serviceType.toLowerCase()) {
            case "minio" -> MinioStorageService.upload(bucketName, objectName, inputStream, contentType);
            case "aws" -> S3StorageService.upload(bucketName, objectName, inputStream, contentType);
            case "azure" -> AzureBlobStorageService.upload(bucketName, objectName, inputStream, contentType);
            case "gcp" -> GoogleCloudStorageService.upload(bucketName, objectName, inputStream, contentType);
            default -> throw new IllegalStateException("Unsupported storage environment: " + serviceType);
        };
    }

    public static InputStream download(String bucketName, String objectName) {
        String serviceType = System.getenv("STORAGE_SERVICE_TYPE");

        return switch (serviceType.toLowerCase()) {
            case "minio" -> MinioStorageService.download(bucketName, objectName);
            case "aws" -> S3StorageService.download(bucketName, objectName);
            case "azure" -> AzureBlobStorageService.download(bucketName, objectName);
            case "gcp" -> GoogleCloudStorageService.download(bucketName, objectName);
            default -> throw new IllegalStateException("Unsupported storage environment: " + serviceType);
        };
    }

    public static InputStream download(String filePath){
        BucketObject bucketObject = FileUtils.extractBucketAndObjectName(filePath);
        return download(bucketObject.getBucketName(),  bucketObject.getObjectName());
    }

//    private static StorageService createS3StorageService(String endpoint, String accessKey, String secretKey, String region) {
//        validateNotEmpty(endpoint, accessKey, secretKey, region);
//
//        S3Client s3Client = S3Client.builder()
//                .endpointOverride(URI.create(endpoint))
//                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
//                .region(Region.of(region))
//                .build();
//
//        return new S3StorageService(s3Client);
//    }
//
//    private static StorageService createAzureBlobStorageService(String accountName, String accountKey) {
//        validateNotEmpty(accountName, accountKey);
//
//        String connectionString = String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net", accountName, accountKey);
//        return new AzureBlobStorageService(connectionString);
//    }
//
//    private static StorageService createGoogleCloudStorageService(String projectId, String credentialsFilePath) {
//        validateNotEmpty(projectId, credentialsFilePath);
//
//        GoogleCredentials credentials = null;
//        try {
//            credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsFilePath))
//                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        Storage storage = StorageOptions.newBuilder()
//                .setCredentials(credentials)
//                .setProjectId(projectId)
//                .build()
//                .getService();
//
//        return new GoogleCloudStorageService(storage);
//    }
}
