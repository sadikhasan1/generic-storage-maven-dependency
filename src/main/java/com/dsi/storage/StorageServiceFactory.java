package com.dsi.storage;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.dsi.storage.azureblob.AzureBlobStorageService;
import com.dsi.storage.core.StorageService;
import com.dsi.storage.googlecloud.GoogleCloudStorageService;
import com.dsi.storage.minio.MinioStorageService;
import com.dsi.storage.s3.S3StorageService;

public class StorageServiceFactory {
    public static StorageService createS3StorageService(String endpoint, String accessKey, String secretKey, String region, String bucketName) {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();

        return new S3StorageService(amazonS3, bucketName);
    }

    public static StorageService createMinioStorageService(String endpoint, String accessKey, String secretKey, String bucketName) {
        return new MinioStorageService(endpoint, accessKey, secretKey, bucketName);
    }

    public static StorageService createAzureBlobStorageService(String connectionString, String containerName) {
        return new AzureBlobStorageService(connectionString, containerName);
    }

    public static StorageService createGoogleCloudStorageService(String projectId, String bucketName) {
        return new GoogleCloudStorageService(projectId, bucketName);
    }
}