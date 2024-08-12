package com.dsi.storage.client.minio;

import com.dsi.storage.client.StorageClient;
import com.dsi.storage.dto.StorageLocation;
import com.dsi.storage.dto.FileData;
import com.dsi.storage.exception.StorageException;
import com.dsi.storage.util.FilePathUtil;
import com.dsi.storage.util.ValidationUtils;
import io.minio.*;
import io.minio.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * MinioStorageService provides methods for interacting with MinIO for file storage and retrieval.
 * The file path structure should be like: <bucketName>/<nestedFolders>/<fileId>.
 * Example: "my-bucket/folder1/folder2/6cbd360f-df93-48eb-901b-87e97a5ddb8e"
 */
public class MinioStorageService implements StorageClient {
    private static final String endpoint = System.getenv("STORAGE_ENDPOINT");
    private static final String accessKey = System.getenv("STORAGE_ACCESS_KEY");
    private static final String secretKey = System.getenv("STORAGE_SECRET_KEY");
    private static final long partSize = (System.getenv("STORAGE_PART_SIZE") != null)
            ? Long.parseLong(System.getenv("STORAGE_PART_SIZE"))
            : 10485760L; // 10 MB default size

    private final MinioClient minioClient;
    private static final Logger logger = LoggerFactory.getLogger(MinioStorageService.class);

    /**
     * Constructs a MinioStorageService instance using environment variables for MinIO configuration.
     */
    public MinioStorageService() {
        ValidationUtils.validateNotEmpty(endpoint, accessKey, secretKey);
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Override
    public String upload(String fullPath, InputStream data, String contentType) throws StorageException {
        try {
            String baseBucket = MinioUtils.extractBaseBucket(fullPath);
            String directoryBucketString = MinioUtils.convertPathToDirectoryBucketString(fullPath);
            String fileId = UUID.randomUUID().toString();
            logger.debug("Base Bucket: {}, Directory Bucket String: {}, Generated File ID: {}", baseBucket, directoryBucketString, fileId);

            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(baseBucket).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(baseBucket).build());
                logger.debug("Bucket '{}' created successfully.", baseBucket);
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(baseBucket)
                            .object(directoryBucketString + "/" + fileId)
                            .stream(data, -1, partSize) // -1 for unknown size
                            .contentType(contentType)
                            .build()
            );

            logger.debug("File uploaded successfully: {}", baseBucket + "/" + directoryBucketString + "/" + fileId);
            return baseBucket + "/" + directoryBucketString + "/" + fileId;
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Failed to upload file to MinIO: {}", e.getMessage());
            throw new StorageException("Failed to upload file to MinIO", e);
        }
    }

    @Override
    public FileData download(String fullPathWithFileId) throws StorageException {
        try {
            StorageLocation storageLocation = FilePathUtil.extractBucketAndObjectName(fullPathWithFileId);
            String contentType = getContentType(minioClient, storageLocation);
            GetObjectResponse response = getObjectResponse(minioClient, storageLocation);

            logger.info("File downloaded successfully: {}", fullPathWithFileId);
            return new FileData(response, contentType);
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Failed to download file from MinIO: {}", e.getMessage());
            throw new StorageException("Failed to download file from MinIO", e);
        }
    }

    private static String getContentType(MinioClient minioClient, StorageLocation storageLocation)
            throws ServerException, InsufficientDataException, ErrorResponseException, IOException,
            NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        StatObjectResponse statObject = minioClient.statObject(StatObjectArgs.builder()
                .bucket(storageLocation.bucketName())
                .object(storageLocation.objectName())
                .build()
        );
        return statObject.contentType();
    }

    private static GetObjectResponse getObjectResponse(MinioClient minioClient, StorageLocation storageLocation)
            throws ServerException, InsufficientDataException, ErrorResponseException, IOException,
            NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(storageLocation.bucketName())
                .object(storageLocation.objectName())
                .build()
        );
    }
}
