package com.dsi.storage.client.minio;

import com.dsi.storage.client.StorageClient;
import com.dsi.storage.dto.FileData;
import com.dsi.storage.exception.StorageException;
import com.dsi.storage.util.PathUtil;
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
        try {
            String[] parts = PathUtil.splitPathForUpload(fullPath);
            String[] extractedParts = extractPathComponents(parts);

            String baseBucket = extractedParts[0];
            String directoryBucketPath = extractedParts[1];

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
            String filePath = baseBucket + "/" + objectPath;

            logger.info("File uploaded successfully: {}", filePath);
            return filePath;
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Failed to upload file to MinIO", e);
            throw new StorageException("Failed to upload file to MinIO", e);
        }
    }

    @Override
    public FileData download(String fullPathWithFileId) throws StorageException {
        try {
            String[] parts = PathUtil.splitPathForDownload(fullPathWithFileId);

            String[] extractedParts = extractPathComponents(parts);

            String baseBucket = extractedParts[0];
            String fileIdWithDirectoryBucketPath = extractedParts[1];

            if (fileIdWithDirectoryBucketPath.isEmpty()) {
                throw new IllegalArgumentException("The file path must contain a valid bucket name and object name.");
            }

            GetObjectResponse response = getObjectResponse(minioClient, baseBucket, fileIdWithDirectoryBucketPath);
            String contentType = response.headers().get("Content-Type");
            if (contentType == null) {
                throw new StorageException("Failed to retrieve content type.");
            }

            return new FileData(response, contentType);
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Failed to download file from MinIO", e);
            throw new StorageException("Failed to download file from MinIO", e);
        }
    }

    private static GetObjectResponse getObjectResponse(MinioClient minioClient, String baseBucket, String fileIdWithDirectoryBucketPath)
            throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(baseBucket)
                .object(fileIdWithDirectoryBucketPath)
                .build()
        );
    }

    private static String[] extractPathComponents(String[] parts) throws StorageException {
        if (parts.length == 0) {
            throw new StorageException("Invalid path: " + String.join("/", parts));
        }

        String baseBucket = parts[0];
        String directoryBucketPath = String.join("/", Arrays.copyOfRange(parts, 1, parts.length));

        return new String[] { baseBucket, directoryBucketPath };
    }
}
