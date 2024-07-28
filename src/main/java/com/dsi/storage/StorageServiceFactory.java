package com.dsi.storage;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.dsi.storage.azureblob.AzureBlobStorageService;
import com.dsi.storage.core.StorageService;
import com.dsi.storage.minio.MinioStorageService;
import com.dsi.storage.s3.S3StorageService;
import io.minio.MinioClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class StorageServiceFactory {

    public static StorageService createStorageService() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = StorageServiceFactory.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IOException("Unable to find application.properties");
            }
            properties.load(input);
        }

        String service = properties.getProperty("storage.service");
        switch (service) {
            case "s3":
                return createS3StorageService(properties);
            case "minio":
                return createMinioStorageService(properties);
            case "azure":
                return createAzureBlobStorageService(properties);
            default:
                throw new IllegalArgumentException("Unknown storage service: " + service);
        }
    }

    private static StorageService createS3StorageService(Properties properties) {
        String endpoint = properties.getProperty("storage.endpoint");
        String accessKey = properties.getProperty("storage.accessKey");
        String secretKey = properties.getProperty("storage.secretKey");
        String region = properties.getProperty("storage.region");
        String bucketName = properties.getProperty("storage.bucketName");

        if (endpoint == null || accessKey == null || secretKey == null || region == null || bucketName == null) {
            throw new IllegalArgumentException("Missing required properties for S3 storage");
        }

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();

        return new S3StorageService(amazonS3, bucketName);
    }

    private static StorageService createMinioStorageService(Properties properties) {
        String endpoint = properties.getProperty("storage.endpoint");
        String accessKey = properties.getProperty("storage.accessKey");
        String secretKey = properties.getProperty("storage.secretKey");
        String bucketName = properties.getProperty("storage.bucketName");

        if (endpoint == null || accessKey == null || secretKey == null || bucketName == null) {
            throw new IllegalArgumentException("Missing required properties for Minio storage");
        }

        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        return new MinioStorageService(endpoint, accessKey, secretKey, bucketName);
    }

    private static StorageService createAzureBlobStorageService(Properties properties) {
        String connectionString = properties.getProperty("storage.connectionString");
        String containerName = properties.getProperty("storage.containerName");

        if (connectionString == null || containerName == null) {
            throw new IllegalArgumentException("Missing required properties for Azure Blob Storage");
        }

        return new AzureBlobStorageService(connectionString, containerName);
    }
}