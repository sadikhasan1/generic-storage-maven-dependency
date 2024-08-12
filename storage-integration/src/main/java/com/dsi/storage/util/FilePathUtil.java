package com.dsi.storage.util;

import com.dsi.storage.dto.StorageLocation;

/**
 * Provides utility methods related to bucket operations.
 */
public class FilePathUtil {
    /**
     * Extracts the bucket name and object name from the file path.
     * @param filePath The full file path including bucket and object name.
     * @return A BucketObject containing the bucket name and object name.
     * @throws IllegalArgumentException If the file path does not contain a valid bucket name or object name.
     */
    public static StorageLocation extractBucketAndObjectName(String filePath) throws IllegalArgumentException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty.");
        }

        int firstSlashIndex = filePath.indexOf('/');

        if (firstSlashIndex != -1) {
            String bucketName = filePath.substring(0, firstSlashIndex);
            String objectName = filePath.substring(firstSlashIndex + 1);

            if (bucketName.trim().isEmpty() || objectName.trim().isEmpty()) {
                throw new IllegalArgumentException("The file path must contain a valid bucket name and object name.");
            }

            return new StorageLocation(bucketName, objectName);
        } else {
            throw new IllegalArgumentException("File path must contain '/' to separate bucket and object name.");
        }
    }
}
