package com.dsi.storage.client.minio;

import java.util.ArrayList;
import java.util.List;

public class MinioUtils {
    // Regular expression for validating MinIO bucket names
    // - Bucket names must consist of lowercase letters, numbers, hyphens, and dots.
    // - Must be between 3 and 63 characters long.
    // - Must start and end with a lowercase letter or number.
    // - Cannot contain two adjacent dots or a dot adjacent to a hyphen.
    // - Cannot be formatted as an IP address.
    // - Cannot start with the prefix 'xn--'.
    // - Cannot end with the suffix '-s3alias'.
    private static final String BUCKET_NAME_REGEX = "^(?!xn--)(?!.*\\.-)(?!.*--)(?!.*\\.\\.)[a-z0-9](?:[a-z0-9\\-]*[a-z0-9])?$";
    private static final int MIN_BUCKET_NAME_LENGTH = 3;
    private static final int MAX_BUCKET_NAME_LENGTH = 63;
    private static final String RESERVED_SUFFIX = "-s3alias";

    // Extracts the base bucket from a full path and validates it
    static String extractBaseBucket(String fullPath) throws IllegalArgumentException {
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
    static String convertPathToDirectoryBucketString(String path) throws IllegalArgumentException {
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

    // Validates a bucket name according to MinIO naming rules
    private static boolean isValidBucketName(String bucketName) {
        if (bucketName.length() < MIN_BUCKET_NAME_LENGTH || bucketName.length() > MAX_BUCKET_NAME_LENGTH) {
            return false;
        }
        if (!bucketName.matches(BUCKET_NAME_REGEX)) {
            return false;
        }
        if (bucketName.endsWith(RESERVED_SUFFIX)) {
            return false;
        }
        if (bucketName.matches("^\\d{1,3}(\\.\\d{1,3}){3}$")) {
            return false; // Check for IP address format
        }
        return true;
    }
}
