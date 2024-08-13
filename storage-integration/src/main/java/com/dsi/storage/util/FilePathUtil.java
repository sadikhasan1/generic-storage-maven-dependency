package com.dsi.storage.util;

import com.dsi.storage.dto.BucketPath;

import java.util.Arrays;

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

    /**
     * Converts a path to a valid directory bucket string and validates each directory.
     * @param path The path to convert.
     * @return A BucketPath object containing the base bucket and the directories path.
     * @throws IllegalArgumentException If the path is null, empty, or contains invalid directory names.
     */
    public static BucketPath splitPathToBaseBucketAndRemainingWithValidation(String path) {
        emptyCheck(path);

        // Normalize the path
        String normalizedPath = normalizePath(path);

        // Handle case where there is no '/'
        if (!normalizedPath.contains("/")) {
            String baseBucket = normalizedPath.trim();
            if (baseBucket.isEmpty() || !isValidBucketName(baseBucket)) {
                throw new IllegalArgumentException("Invalid base bucket name: " + baseBucket);
            }
            return new BucketPath(baseBucket, "");
        }

        // Validate the entire normalized path
        if (!isValidPath(normalizedPath)) {
            throw new IllegalArgumentException("Invalid directory path: " + normalizedPath);
        }

        // Split normalized path into base bucket and directory path
        int firstSlashIndex = normalizedPath.indexOf('/');
        String baseBucket = normalizedPath.substring(0, firstSlashIndex).trim();
        String directoryPath = normalizedPath.substring(firstSlashIndex + 1).trim(); // Handle empty directoryPath

        // Return the valid BucketPath
        return new BucketPath(baseBucket, directoryPath);
    }

    /**
     * Extracts the bucket name and object name from the file path.
     * @param filePath The full file path including bucket and object name.
     * @return A BucketPath containing the bucket name and object name.
     * @throws IllegalArgumentException If the file path does not contain a valid bucket name or object name.
     */
    public static BucketPath splitFilePathToBaseBucketAndRemaining(String filePath) {
        emptyCheck(filePath);

        int firstSlashIndex = filePath.indexOf('/');
        if (firstSlashIndex == -1) {
            throw new IllegalArgumentException("File path must contain '/' to separate bucket and object name.");
        }

        String bucketName = filePath.substring(0, firstSlashIndex).trim();
        String objectName = filePath.substring(firstSlashIndex + 1).trim();

        if (bucketName.isEmpty() || objectName.isEmpty()) {
            throw new IllegalArgumentException("The file path must contain a valid bucket name and object name.");
        }

        return new BucketPath(bucketName, objectName);
    }

    /**
     * Validates the provided path for null or empty values.
     * @param path The path to validate.
     * @throws IllegalArgumentException If the path is null or empty.
     */
    private static void emptyCheck(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }
    }

    /**
     * Normalizes the path by trimming, replacing multiple slashes with a single slash, replacing spaces with hyphens,
     * removing leading and trailing slashes.
     * @param path The path to normalize.
     * @return The normalized path.
     */
    private static String normalizePath(String path) {
        return path.trim()
                .replaceAll("/+", "/") // Replace multiple slashes with a single slash
                .replaceAll(" ", "-") // Replace spaces with hyphens
                .replaceAll("^/|/$", ""); // Remove leading and trailing slashes
    }

    /**
     * Validates a bucket name according to general naming rules.
     * @param bucketName The bucket name to validate.
     * @return True if the bucket name is valid, false otherwise.
     */
    private static boolean isValidBucketName(String bucketName) {
        if (bucketName.length() < MIN_BUCKET_NAME_LENGTH || bucketName.length() > MAX_BUCKET_NAME_LENGTH) {
            return false;
        }

        // Determine the storage service type and apply service-specific validation
        switch (STORAGE_SERVICE_TYPE.toLowerCase()) {
            case "minio":
                return isValidMinioBucketName(bucketName);
            default:
                return true;
        }
    }

    /**
     * Validates a bucket name according to MinIO-specific naming rules.
     * @param bucketName The bucket name to validate.
     * @return True if the bucket name is valid, false otherwise.
     */
    private static boolean isValidMinioBucketName(String bucketName) {
        return bucketName.matches(MINIO_BUCKET_NAME_REGEX) &&
                !bucketName.endsWith(RESERVED_SUFFIX) &&
                !bucketName.matches("^\\d{1,3}(\\.\\d{1,3}){3}$");
    }

    /**
     * Validates the directory path by checking each segment.
     * @param directoryPath The directory path to validate.
     * @return True if the directory path is valid, false otherwise.
     */
    private static boolean isValidPath(String directoryPath) {
        return Arrays.stream(directoryPath.split("/"))
                .allMatch(dir -> dir.isEmpty() || isValidBucketName(dir));
    }
}
