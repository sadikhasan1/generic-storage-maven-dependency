package com.dsi.storage.minio;

import com.dsi.storage.client.StorageClient;
import com.dsi.storage.dto.BucketObject;
import com.dsi.storage.dto.FileData;
import com.dsi.storage.exception.StorageException;
import com.dsi.storage.util.BucketUtils;
import com.dsi.storage.util.FileNameUtils;
import io.minio.*;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.IOException;

/**
 * MinioStorageService provides methods for interacting with MinIO for file storage and retrieval.
 * The file path structure should be like: <bucketName>/<nestedFolders>/<fileId>.
 * Example: "my-bucket/folder1/folder2/6cbd360f-df93-48eb-901b-87e97a5ddb8e"
 */
public class MinioStorageService implements StorageClient {
    private static final Logger logger = LoggerFactory.getLogger(MinioStorageService.class);

    private final MinioClient minioClient;
    private final long partSize;

    /**
     * Constructs a MinioStorageService instance using environment variables for MinIO configuration.
     */
    public MinioStorageService() {
        this.minioClient = MinioClientFactory.createMinioClient();
        this.partSize = MinioClientFactory.getUploadPartSize();
    }



    @Override
    public String upload(String fullPath, InputStream data, String contentType) throws StorageException {
        try {
            String fileIdWithNestedFolders = FileNameUtils.generateUniqueFileIdWithNestedFolders(fullPath);
            String baseBucketName = BucketUtils.getBaseBucketName(fullPath);

            logger.info("Uploading file with ID {} to bucket '{}'", fileIdWithNestedFolders, baseBucketName);

            MinioUtils.ensureBucketExists(minioClient, baseBucketName);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(baseBucketName)
                            .object(fileIdWithNestedFolders)
                            .stream(data, -1, partSize) // -1 for unknown size
                            .contentType(contentType)
                            .build()
            );

            logger.info("File uploaded successfully: {}", fileIdWithNestedFolders);
            return baseBucketName + "/" + fileIdWithNestedFolders;
        } catch (MinioException e) {
            throw MinioExceptions.handleMinioException("upload", e);
        } catch (IOException e) {
            throw MinioExceptions.handleIOException("upload", e);
        } catch (Exception e) {
            throw MinioExceptions.handleUnexpectedException("upload", e);
        }
    }



    @Override
    public FileData download(String fullPathWithFileId) throws StorageException {
        try {
            // Extract bucket and object name
            BucketObject bucketObject = BucketUtils.extractBucketAndObjectName(fullPathWithFileId);

            // Retrieve the content type and object response
            String contentType = MinioUtils.getContentType(minioClient, bucketObject);
            GetObjectResponse response = MinioUtils.getObjectResponse(minioClient, bucketObject);

            // Log the download success
            logger.info("File downloaded successfully: {}", fullPathWithFileId);

            // Return the FileData object without closing the InputStream
            return new FileData(response, contentType);
        } catch (MinioException e) {
            throw MinioExceptions.handleMinioException("download", e);
        } catch (IOException e) {
            throw MinioExceptions.handleIOException("download", e);
        } catch (Exception e) {
            throw MinioExceptions.handleUnexpectedException("download", e);
        }
    }






}
