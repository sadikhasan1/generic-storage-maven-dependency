package com.dsi.storage.core;

import java.io.InputStream;

import com.dsi.storage.azureblob.AzureBlobStorageService;
import com.dsi.storage.googlecloud.GoogleCloudStorageService;
import com.dsi.storage.minio.MinioStorageService;
import com.dsi.storage.s3.S3StorageService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.minio.MinioClient;
import org.primefaces.model.file.UploadedFile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public abstract class StorageService {
    public abstract String upload(String bucketName, String objectName, InputStream inputStream, String contentType);
    public abstract String upload(String bucketName, UploadedFile uploadedFile);
    public abstract String upload(String bucketName, MultipartFile file);
    public abstract InputStream download(String filePath);
    public abstract InputStream download(String bucketName, String objectName);
    public abstract InputStreamResource downloadInputStreamResource(String filePath);
    public abstract UploadedFile downloadAsUploadedFile(String filePath);
    public abstract MultipartFile downloadAsMultipartFile(String filePath);
    public abstract ResponseEntity<InputStreamResource> downloadAsResponseEntityForInputStreamResource(String filePath);
    public abstract ResponseEntity<Resource> downloadAsResponseEntityForResource(String filePath);

    public static StorageService init() {
        String serviceType = System.getenv("STORAGE_SERVICE_TYPE");
        String endpoint = System.getenv("STORAGE_ENDPOINT");
        String accessKey = System.getenv("STORAGE_ACCESS_KEY");
        String secretKey = System.getenv("STORAGE_SECRET_KEY");
        String region = System.getenv("STORAGE_REGION");
        String accountName = System.getenv("STORAGE_ACCOUNT_NAME");
        String accountKey = System.getenv("STORAGE_ACCOUNT_KEY");
        String projectId = System.getenv("STORAGE_PROJECT_ID");
        String credentialsFilePath = System.getenv("STORAGE_CREDENTIALS_FILE_PATH");


        return switch (serviceType.toLowerCase()) {
            case "s3" -> createS3StorageService(endpoint, accessKey, secretKey, region);
            case "minio" -> createMinioStorageService(endpoint, accessKey, secretKey);
            case "azure" -> createAzureBlobStorageService(accountName, accountKey);
            case "gcs" -> createGoogleCloudStorageService(projectId, credentialsFilePath);
            default -> throw new IllegalArgumentException("Unknown storage service type: " + serviceType);
        };
    }

    private static StorageService createS3StorageService(String endpoint, String accessKey, String secretKey, String region) {
        validateNotEmpty(endpoint, accessKey, secretKey, region);

        S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .build();

        return new S3StorageService(s3Client);
    }

    private static StorageService createMinioStorageService(String endpoint, String accessKey, String secretKey) {
        validateNotEmpty(endpoint, accessKey, secretKey);

        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        return new MinioStorageService(minioClient);
    }

    private static StorageService createAzureBlobStorageService(String accountName, String accountKey) {
        validateNotEmpty(accountName, accountKey);

        String connectionString = String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net", accountName, accountKey);
        return new AzureBlobStorageService(connectionString);
    }

    private static StorageService createGoogleCloudStorageService(String projectId, String credentialsFilePath) {
        validateNotEmpty(projectId, credentialsFilePath);

        GoogleCredentials credentials = null;
        try {
            credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsFilePath))
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
