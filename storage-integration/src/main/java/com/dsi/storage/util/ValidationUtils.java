package com.dsi.storage.util;

import java.util.Arrays;

/**
 * Provides utility methods for validating input parameters.
 */
public class ValidationUtils {
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
     * Validates that none of the provided "required" parameters are null or empty.
     * @param params The parameters to validate.
     * @throws IllegalArgumentException If any of the parameters are null or empty.
     */
    public static void emptyCheckOnRequiredFields(String... params) throws IllegalArgumentException {
        StringBuilder errorMessage = new StringBuilder("The following parameters are invalid: ");
        boolean hasInvalidParams = false;

        for (int i = 0; i < params.length; i++) {
            if (isNullOrEmpty(params[i])) {
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

    /**
     * Validates that the provided path is neither null nor empty.
     * @param path The path to validate.
     * @throws IllegalArgumentException If the path is null or empty.
     */
    public static boolean isNullOrEmpty(String path) {
        return (path == null || path.trim().isEmpty());
    }

    /**
     * Validates a bucket or directory name according to the current storage service type.
     * @param name The bucket or directory name to validate.
     * @return True if the name is valid, false otherwise.
     */
    public static boolean isValidBucketName(String name) {
        if (name.length() < MIN_BUCKET_NAME_LENGTH || name.length() > MAX_BUCKET_NAME_LENGTH) {
            return false;
        }

        switch (STORAGE_SERVICE_TYPE.toLowerCase()) {
            case "minio":
                return isValidMinioBucketName(name);
            default:
                return true;
        }
    }

    /**
     * Validates a bucket name according to MinIO-specific naming rules.
     * @param bucketName The bucket name to validate.
     * @return True if the bucket name is valid, false otherwise.
     */
    public static boolean isValidMinioBucketName(String bucketName) {
        return bucketName.matches(MINIO_BUCKET_NAME_REGEX) &&
                !bucketName.endsWith(RESERVED_SUFFIX) &&
                !bucketName.matches("^\\d{1,3}(\\.\\d{1,3}){3}$");
    }

    /**
     * Validates the directory path by checking each segment.
     * @param parts The directory path segments to validate.
     * @return True if all path segments are valid, false otherwise.
     */
    public static boolean isValidPath(String[] parts) {
        return Arrays.stream(parts)
                .allMatch(dir -> !dir.isEmpty() && isValidBucketName(dir));
    }
}
