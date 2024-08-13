package com.dsi.storage.client.minio;

import com.dsi.storage.client.StorageClient;
import com.dsi.storage.dto.FileData;
import com.dsi.storage.exception.StorageException;
import com.dsi.storage.util.ValidationUtils;
import io.minio.*;
import io.minio.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
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

    public MinioStorageService(String endpoint, String accessKey, String secretKey, long partSize) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.partSize = partSize;
    }

    @Override
    public String upload(String fullPath, InputStream data, String contentType) throws StorageException {
        if (ValidationUtils.isNullOrEmpty(fullPath) || data == null || ValidationUtils.isNullOrEmpty(contentType)) {
            logger.error("Upload Path, data stream, or content type cannot be null or empty");
            throw new StorageException("Upload Path, data stream, or content type cannot be null or empty");
        }

        try {
            fullPath = fullPath.trim().replaceAll("/+", "/")  // Replace multiple slashes with a single slash
                    .replaceAll(" ", "-")    // Replace spaces with hyphens
                    .replaceAll("^/|/$", ""); // Remove leading and trailing slashes

            String[] parts = fullPath.split("/");
            // Validate each segment of the path
            if (!ValidationUtils.isValidPath(parts)) {
                throw new IllegalArgumentException("Invalid directory path: " + fullPath);
            }
            String baseBucket = parts[0];
            String directoryBucketPath = String.join("/", Arrays.copyOfRange(parts, 1, parts.length));
            String fileId = UUID.randomUUID().toString();
            logger.debug("Base Bucket: {}, Directory Bucket Path: {}, Generated File ID: {}", baseBucket, directoryBucketPath, fileId);

            // Create bucket if it does not exist
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(baseBucket).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(baseBucket).build());
                logger.debug("Bucket '{}' created successfully.", baseBucket);
            }

            // Constructing the object path, which will just be the fileId if directoryBucketPath is empty
            String objectPath = directoryBucketPath.isEmpty() ? fileId : directoryBucketPath + "/" + fileId;

            // Upload the file
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(baseBucket)
                            .object(objectPath)
                            .stream(data, -1, partSize) // -1 for unknown size
                            .contentType(contentType)
                            .build()
            );

            // Constructing the full file path
            String filePath = directoryBucketPath.isEmpty() ? baseBucket + "/" + fileId : baseBucket + "/" + objectPath;

            logger.info("File uploaded successfully: {}", filePath);
            return filePath;
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Failed to upload file to MinIO", e);
            throw new StorageException("Failed to upload file to MinIO", e);
        }
    }

    @Override
    public FileData download(String fullPathWithFileId) throws StorageException {
        if (ValidationUtils.isNullOrEmpty(fullPathWithFileId)) {
            logger.error("Download path cannot be null or empty");
            throw new StorageException("Download path cannot be null or empty");
        }

        try {
            fullPathWithFileId = fullPathWithFileId.trim().replaceAll("/+", "/")  // Replace multiple slashes with a single slash
                    .replaceAll("^/|/$", ""); // Remove leading and trailing slashes

            String[] parts = fullPathWithFileId.split("/");
            String baseBucket = parts[0];
            String fileIdWithDirectoryBucketPath = String.join("/", Arrays.copyOfRange(parts, 1, parts.length));

            if (baseBucket.isEmpty() || fileIdWithDirectoryBucketPath.isEmpty()) {
                throw new IllegalArgumentException("The file path must contain a valid bucket name and object name.");
            }

            String contentType = getContentType(minioClient, baseBucket, fileIdWithDirectoryBucketPath);
            GetObjectResponse response = getObjectResponse(minioClient, baseBucket, fileIdWithDirectoryBucketPath);
            return new FileData(response, contentType);
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Failed to download file from MinIO", e);
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
