package com.dsi.storage.util;

import com.dsi.storage.dto.StorageLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides utility methods related to bucket, directory, filePath operations.
 */
public class FilePathUtil {
    // Regular expression for validating MinIO bucket names:
    // 1. (?!xn--) - Ensures the bucket name does not start with the prefix 'xn--'.
    // 2. (?!.*\\.-) - Ensures there is no '.' followed by a '-' anywhere in the name.
    // 3. (?!.*--) - Ensures there are no consecutive hyphens '--' anywhere in the name.
    // 4. (?!.*\\.\\.) - Ensures there are no consecutive dots '..' anywhere in the name.
    // 5. [a-z0-9] - The bucket name must start with a lowercase letter or digit.
    // 6. (?:[a-z0-9\\-]*[a-z0-9])? - The rest of the name can contain lowercase letters, digits, or hyphens, but must end with a lowercase letter or digit.
    private static final String MINIO_BUCKET_NAME_REGEX = "^(?!xn--)(?!.*\\.-)(?!.*--)(?!.*\\.\\.)[a-z0-9](?:[a-z0-9\\-]*[a-z0-9])?$";

    private static final int MIN_BUCKET_NAME_LENGTH = 3;
    private static final int MAX_BUCKET_NAME_LENGTH = 63;
    private static final String RESERVED_SUFFIX = "-s3alias";

    private static final String STORAGE_SERVICE_TYPE = System.getenv("STORAGE_SERVICE_TYPE");

    // Extracts the base bucket from a full path and validates it
    public static String extractBaseBucket(String fullPath) throws IllegalArgumentException {
        if (fullPath == null || fullPath.isEmpty()) {
            throw new IllegalArgumentException("Full path cannot be null or empty");
        }

        String[] parts = fullPath.split("/", 2);
        if (parts.length < 1) {
            throw new IllegalArgumentException("Invalid path format");
        }

        String bucketName = parts[0].trim();

        if (!isValidBucketName(bucketName)) {
            throw new IllegalArgumentException("Invalid bucket name: " + bucketName);
        }

        return bucketName;
    }

    // Converts a path to a valid directory bucket string and validates each directory
    public static String convertPathToDirectoryBucketString(String path) throws IllegalArgumentException {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }

        // Normalize the path: trim, replace multiple slashes with a single slash, replace spaces with hyphens
        String normalizedPath = path.trim()
                .replaceAll("/+", "/") // Replace multiple slashes with a single slash
                .replaceAll(" ", "-"); // Replace spaces with hyphens

        // Remove trailing slash if present
        if (normalizedPath.endsWith("/")) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
        }

        // Extract and validate the base bucket
        String baseBucket = extractBaseBucket(normalizedPath);

        // Extract directories part
        String directoriesPath = normalizedPath.substring(baseBucket.length()).replaceFirst("^/", "");
        if (directoriesPath.isEmpty()) {
            return ""; // No directories
        }

        String[] directories = directoriesPath.split("/");
        List<String> validBuckets = new ArrayList<>();

        for (String dir : directories) {
            if (!dir.isEmpty() && isValidBucketName(dir)) {
                validBuckets.add(dir);
            } else if (!dir.isEmpty()) {
                throw new IllegalArgumentException("Invalid directory bucket name: " + dir);
            }
        }

        return String.join("/", validBuckets);
    }

    // Validates a bucket name according to general naming rules
    private static boolean isValidBucketName(String bucketName) {
        if (bucketName.length() < MIN_BUCKET_NAME_LENGTH || bucketName.length() > MAX_BUCKET_NAME_LENGTH) {
            return false;
        }

        // Determine the storage service type and apply service-specific validation
        switch (STORAGE_SERVICE_TYPE.toLowerCase()) {
            case "minio":
                if (!isValidMinioBucketName(bucketName)) {
                    return false;
                }
                break;
            default:
                break;
        }

        return true;
    }

    // Validates a bucket name according to MinIO-specific naming rules
    private static boolean isValidMinioBucketName(String bucketName) {
        // Check against MinIO-specific regular expression
        if (!bucketName.matches(MINIO_BUCKET_NAME_REGEX)) {
            return false;
        } else if (bucketName.endsWith(RESERVED_SUFFIX)) {
            return false;
        } else return !bucketName.matches("^\\d{1,3}(\\.\\d{1,3}){3}$");
    }


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
