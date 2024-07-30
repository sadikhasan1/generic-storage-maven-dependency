package com.dsi.storage;

import com.dsi.storage.azureblob.AzureBlobStorageService;
import com.dsi.storage.config.StorageConfig;
import com.dsi.storage.core.StorageService;
import com.dsi.storage.googlecloud.GoogleCloudStorageService;
import com.dsi.storage.minio.MinioStorageService;
import com.dsi.storage.s3.S3StorageService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.minio.MinioClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class StorageServiceFactory {

    public static StorageService createStorageService(StorageConfig config) throws IOException {
        return switch (config.getServiceType().toLowerCase()) {
            case "s3" -> createS3StorageService(config.getEndpoint(), config.getAccessKey(), config.getSecretKey(), config.getRegion());
            case "minio" -> createMinioStorageService(config.getEndpoint(), config.getAccessKey(), config.getSecretKey());
            case "azure" -> createAzureBlobStorageService(config.getAccountName(), config.getAccountKey());
            case "gcs" -> createGoogleCloudStorageService(config.getProjectId(), config.getCredentialsFilePath());
            default -> throw new IllegalArgumentException("Unknown storage service type: " + config.getServiceType());
        };
    }

    public static S3StorageService createS3StorageService(String endpoint, String accessKey, String secretKey, String region) {
        validateNotEmpty(endpoint, accessKey, secretKey, region);

        S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .build();

        return new S3StorageService(s3Client);
    }

    public static StorageService createMinioStorageService(String endpoint, String accessKey, String secretKey) {
        validateNotEmpty(endpoint, accessKey, secretKey);

        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        return new MinioStorageService(minioClient);
    }

    public static StorageService createAzureBlobStorageService(String accountName, String accountKey) {
        validateNotEmpty(accountName, accountKey);

        String connectionString = String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net", accountName, accountKey);
        return new AzureBlobStorageService(connectionString);
    }

    public static StorageService createGoogleCloudStorageService(String projectId, String credentialsFilePath) throws IOException {
        validateNotEmpty(projectId, credentialsFilePath);

        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsFilePath))
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build()
                .getService();

        return new GoogleCloudStorageService(storage);
    }

    private static void validateNotEmpty(String... params) {
        StringBuilder errorMessage = new StringBuilder("The following parameters are invalid: ");
        boolean hasInvalidParams = false;

        for (int i = 0; i < params.length; i++) {
            if (params[i] == null || params[i].isEmpty()) {
                if (hasInvalidParams) {
                    errorMessage.append(", ");
                }
                errorMessage.append("Parameter ").append(i + 1);
                hasInvalidParams = true;
            }
        }

        if (hasInvalidParams) {
            throw new IllegalArgumentException(errorMessage.toString());
        }
    }
}