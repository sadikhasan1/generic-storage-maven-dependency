package com.dsi.storage.core;

import java.io.InputStream;
import com.dsi.storage.client.StorageClient;
import com.dsi.storage.dto.FileData;
import com.dsi.storage.exception.StorageException;
import com.dsi.storage.minio.MinioStorageService;

/**
 * StorageService provides a unified interface for file storage operations.
 * It delegates operations to the appropriate implementation of StorageClient.
 * Currently, it supports MinIO as the storage service.
 */
public class StorageService {
    private final StorageClient storageClient;

    /**
     * Constructs a StorageService instance based on the environment configuration.
     * Determines the storage client implementation to use (e.g., minio).
     * @throws IllegalStateException If the specified storage service type is unsupported.
     */
    public StorageService() {
        String serviceType = System.getenv("STORAGE_SERVICE_TYPE");

        if (serviceType != null && serviceType.equalsIgnoreCase("minio")) {
            this.storageClient = new MinioStorageService();
        } else {
            // Throw exception for unsupported service types
            throw new IllegalStateException("Unsupported storage environment: " + serviceType);
        }
    }

    /**
     * Uploads a file to the storage service.
     * Delegates the upload operation to the underlying StorageClient implementation.
     * Example:
     * - If `fullPath` is "my-bucket/folder1/folder2", the method will append a unique file ID and upload the file.
     * - The returned path will be something like "my-bucket/folder1/folder2/6cbd360f-df93-48eb-901b-87e97a5ddb8e".
     * @param fullPath The base path where the file will be stored, excluding the file ID.
     *                 Example: "my-bucket/folder1/folder2".
     * @param inputStream The input stream containing the file data.
     * @param contentType The MIME type of the file.
     * @return The full path of the uploaded file, including the generated file ID.
     *         Example: "my-bucket/folder1/folder2/6cbd360f-df93-48eb-901b-87e97a5ddb8e".
     * @throws StorageException If an error occurs during file upload.
     */
    public String upload(String fullPath, InputStream inputStream, String contentType) throws StorageException {
        return storageClient.upload(fullPath, inputStream, contentType);
    }

    /**
     * Downloads a file from the storage service.
     * Delegates the download operation to the underlying StorageClient implementation.
     * Example:
     * - If `fullPathWithFileId` is "my-bucket/folder1/folder2/6cbd360f-df93-48eb-901b-87e97a5ddb8e", the method will download the file from this path.
     * @param fullPathWithFileId The full path of the file to be downloaded, including the file ID.
     *                           Example: "my-bucket/folder1/folder2/6cbd360f-df93-48eb-901b-87e97a5ddb8e".
     * @return FileData containing the file's InputStream and content type.
     * @throws StorageException If an error occurs during file download.
     */
    public FileData download(String fullPathWithFileId) throws StorageException {
        return storageClient.download(fullPathWithFileId);
    }
}
