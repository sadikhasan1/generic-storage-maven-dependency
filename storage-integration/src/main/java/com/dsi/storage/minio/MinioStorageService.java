package com.dsi.storage.minio;

import com.dsi.storage.client.StorageClient;
import com.dsi.storage.dto.BucketObject;
import com.dsi.storage.dto.FileData;
import com.dsi.storage.exception.StorageException;
import com.dsi.storage.util.BucketUtils;
import com.dsi.storage.util.FileNameUtils;
import com.dsi.storage.util.ValidationUtils;
import io.minio.*;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * MinioStorageService provides methods for interacting with MinIO for file storage and retrieval.
 * The file path structure should be like: <bucketName>/<nestedFolders>/<fileId>.
 * Example: "my-bucket/folder1/folder2/6cbd360f-df93-48eb-901b-87e97a5ddb8e"
 */
public class MinioStorageService implements StorageClient {
    private static final Logger logger = LoggerFactory.getLogger(MinioStorageService.class);

    private final MinioClient minioClient;
    private final long uploadPartSize;

    /**
     * Constructs a MinioStorageService instance using environment variables for MinIO configuration.
     * @throws RuntimeException If there is a configuration issue.
     */
    public MinioStorageService() {
        String endpoint = System.getenv("STORAGE_ENDPOINT");
        String accessKey = System.getenv("STORAGE_ACCESS_KEY");
        String secretKey = System.getenv("STORAGE_SECRET_KEY");
        String partSize = System.getenv("STORAGE_PART_SIZE");

        validateEnvironmentVariables(endpoint, accessKey, secretKey);

        this.minioClient = createMinioClient(endpoint, accessKey, secretKey);
        this.uploadPartSize = getUploadPartSize(partSize);
    }

    private void validateEnvironmentVariables(String endpoint, String accessKey, String secretKey) {
        try {
            ValidationUtils.validateNotEmpty(endpoint, accessKey, secretKey);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Environment variables for MinIO configuration are missing or invalid", e);
        }
    }

    private MinioClient createMinioClient(String endpoint, String accessKey, String secretKey) {
        try {
            return MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing MinIO client", e);
        }
    }

    private long getUploadPartSize(String partSizeEnv) {
        long defaultPartSize = 10485760L; // 10 MB default size

        if (partSizeEnv != null) {
            try {
                return Long.parseLong(partSizeEnv);
            } catch (NumberFormatException e) {
                logger.warn("Invalid STORAGE_PART_SIZE value, using default.", e);
            }
        }
        return defaultPartSize;
    }

    @Override
    public String upload(String fullPath, InputStream data, String contentType) throws StorageException {
        try {
            String fileIdWithNestedFolders = FileNameUtils.generateUniqueFileIdWithNestedFolders(fullPath);
            String baseBucketName = BucketUtils.getBaseBucketName(fullPath);

            logger.info("Uploading file with ID {} to bucket '{}'", fileIdWithNestedFolders, baseBucketName);

            ensureBucketExists(baseBucketName);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(baseBucketName)
                            .object(fileIdWithNestedFolders)
                            .stream(data, -1, uploadPartSize) // -1 for unknown size
                            .contentType(contentType)
                            .build()
            );

            logger.info("File uploaded successfully: {}", fileIdWithNestedFolders);
            return baseBucketName + "/" + fileIdWithNestedFolders;
        } catch (MinioException e) {
            throw handleMinioException("upload", e);
        } catch (IOException e) {
            throw handleIOException("upload", e);
        } catch (Exception e) {
            throw handleUnexpectedException("upload", e);
        }
    }

    private void ensureBucketExists(String baseBucketName) throws StorageException {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(baseBucketName).build());
            if (!bucketExists) {
                // Create the bucket if it doesn't exist
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(baseBucketName).build());
                logger.info("Bucket '{}' created successfully.", baseBucketName);
            }
        } catch (MinioException e) {
            throw handleMinioException("ensureBucketExists", e);
        } catch (InvalidKeyException e) {
            logger.error("Invalid key while ensuring bucket existence: {}", e.getMessage());
            throw new StorageException("Invalid key while ensuring bucket existence", e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("No such algorithm while ensuring bucket existence: {}", e.getMessage());
            throw new StorageException("No such algorithm while ensuring bucket existence", e);
        } catch (IOException e) {
            logger.error("IO error while ensuring bucket existence: {}", e.getMessage());
            throw new StorageException("IO error while ensuring bucket existence", e);
        } catch (Exception e) {
            throw handleUnexpectedException("ensureBucketExists", e);
        }
    }


    @Override
    public FileData download(String fullPathWithFileId) throws StorageException {
        try {
            BucketObject bucketObject = BucketUtils.extractBucketAndObjectName(fullPathWithFileId);

            GetObjectResponse response = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketObject.bucketName())
                            .object(bucketObject.objectName())
                            .build()
            );

            logger.info("File downloaded successfully: {}", fullPathWithFileId);
            return new FileData(response, response.headers().get("Content-Type"));
        } catch (MinioException e) {
            throw handleMinioException("download", e);
        } catch (IOException e) {
            throw handleIOException("download", e);
        } catch (Exception e) {
            throw handleUnexpectedException("download", e);
        }
    }

    private StorageException handleMinioException(String operation, MinioException e) {
        logger.error("MinIO error while {}: {}", operation, e.getMessage());
        return new StorageException("MinIO error while " + operation, e);
    }

    private StorageException handleIOException(String operation, IOException e) {
        logger.error("IO error while {}: {}", operation, e.getMessage());
        return new StorageException("IO error during " + operation, e);
    }

    private StorageException handleUnexpectedException(String operation, Exception e) {
        logger.error("Unexpected error while {}: {}", operation, e.getMessage());
        return new StorageException("Unexpected error during " + operation, e);
    }
}
