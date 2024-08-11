package com.dsi.storage.util;

import com.dsi.storage.dto.BucketObject;

import java.util.UUID;

public class FileUtils {
    /**
     * Extracts the base bucket name from a full path.
     * @param path The full path including the bucket and folders.
     * @return The base bucket name.
     * @throws IllegalArgumentException If the path is null, empty, or does not contain a valid bucket name.
     */
    public static String getBaseBucketName(String path) throws IllegalArgumentException {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("The input path cannot be null or empty.");
        }

        String[] parts = path.split("/");

        if (parts.length == 0 || parts[0].trim().isEmpty()) {
            throw new IllegalArgumentException("The path does not contain a valid bucket name.");
        }

        return parts[0];
    }

    /**
     * Generates a unique file ID and appends it to the nested folder path.
     * @param fullPath The full path including bucket and nested folders.
     * @return The new file name with the unique ID.
     */
    public static String generateUniqueFileIdWithNestedFolders(String fullPath) {
        if (fullPath == null || fullPath.trim().isEmpty()) {
            throw new IllegalArgumentException("The fullPath cannot be null or empty.");
        }

        String uniqueId = UUID.randomUUID().toString();
        String nestedFolders = fullPath.contains("/")
                ? fullPath.substring(fullPath.indexOf('/') + 1)
                : "";

        String fileName = nestedFolders.isEmpty()
                ? uniqueId
                : nestedFolders + "/" + uniqueId;

        return fileName.replaceAll("/+", "/").replaceFirst("^/", "");
    }

    /**
     * Extracts the bucket name and object name from the file path.
     * @param filePath The full file path including bucket and object name.
     * @return A BucketObject containing the bucket name and object name.
     * @throws IllegalArgumentException If the file path does not contain a valid bucket name or object name.
     */
    public static BucketObject extractBucketAndObjectName(String filePath) throws IllegalArgumentException {
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

            return new BucketObject(bucketName, objectName);
        } else {
            throw new IllegalArgumentException("File path must contain '/' to separate bucket and object name.");
        }
    }

    /**
     * Validates that none of the provided parameters are null or empty.
     * @param params The parameters to validate.
     * @throws IllegalArgumentException If any of the parameters are null or empty.
     */
    public static void validateNotEmpty(String... params) throws IllegalArgumentException {
        StringBuilder errorMessage = new StringBuilder("The following parameters are invalid: ");
        boolean hasInvalidParams = false;

        for (int i = 0; i < params.length; i++) {
            if (params[i] == null || params[i].trim().isEmpty()) {
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
