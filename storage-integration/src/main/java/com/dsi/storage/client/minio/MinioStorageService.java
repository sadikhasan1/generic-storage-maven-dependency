package com.dsi.storage.client.minio;

import com.dsi.storage.client.StorageClient;
import com.dsi.storage.dto.BucketPath;
import com.dsi.storage.dto.FileData;
import com.dsi.storage.exception.StorageException;
import com.dsi.storage.util.FilePathUtil;
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
    private final long partSize;
    private final MinioClient minioClient;
    private static final Logger logger = LoggerFactory.getLogger(MinioStorageService.class);

    public MinioStorageService(MinioClient minioClient, long partSize) {
        this.minioClient = minioClient;
        this.partSize = partSize;
    }

    @Override
    public String upload(String fullPath, InputStream data, String contentType) throws StorageException {
        try {
            BucketPath bucketPath = FilePathUtil.splitPathToBaseBucketAndRemainingWithValidation(fullPath);
            String baseBucket = bucketPath.baseBucket();
            String directoryBucketPath = bucketPath.remainingPath();
            String fileId = UUID.randomUUID().toString();
            logger.debug("Base Bucket: {}, Directory Bucket String: {}, Generated File ID: {}", baseBucket, directoryBucketPath, fileId);

            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(baseBucket).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(baseBucket).build());
                logger.debug("Bucket '{}' created successfully.", baseBucket);
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(baseBucket)
                            .object(directoryBucketPath + "/" + fileId)
                            .stream(data, -1, partSize) // -1 for unknown size
                            .contentType(contentType)
                            .build()
            );

            String filePath = baseBucket +
                    (directoryBucketPath.isEmpty() ? "" : "/" + directoryBucketPath) +
                    "/" + fileId;
            logger.info("File uploaded successfully: {}", filePath);
            return filePath;
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Failed to upload file to MinIO: {}", e.getMessage());
            throw new StorageException("Failed to upload file to MinIO", e);
        }
    }

    @Override
    public FileData download(String fullPathWithFileId) throws StorageException {
        try {
            BucketPath bucketPath = FilePathUtil.splitFilePathToBaseBucketAndRemaining(fullPathWithFileId);
            String baseBucket = bucketPath.baseBucket();
            String fileIdWithDirectoryBucketPath = bucketPath.remainingPath();
            String contentType = getContentType(minioClient, baseBucket, fileIdWithDirectoryBucketPath);
            GetObjectResponse response = getObjectResponse(minioClient, baseBucket, fileIdWithDirectoryBucketPath);

            logger.info("File downloaded successfully: {}", fullPathWithFileId);
            return new FileData(response, contentType);
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Failed to download file from MinIO: {}", e.getMessage());
            throw new StorageException("Failed to download file from MinIO", e);
        }
    }

    private static String getContentType(MinioClient minioClient, String baseBucket, String fileIdWithDirectoryBucketPath)
            throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        StatObjectResponse statObject = minioClient.statObject(StatObjectArgs.builder()
                .bucket(baseBucket)
                .object(fileIdWithDirectoryBucketPath)
                .build()
        );
        return statObject.contentType();
    }

    private static GetObjectResponse getObjectResponse(MinioClient minioClient, String baseBucket, String fileIdWithDirectoryBucketPath)
            throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(baseBucket)
                .object(fileIdWithDirectoryBucketPath)
                .build()
        );
    }
}
