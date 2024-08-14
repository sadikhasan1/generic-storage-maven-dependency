package com.dsi.storage.util;

import java.util.Arrays;

/**
 * Provides utility methods for validating input parameters.
 */
public class ValidationUtils {

    private static final int MIN_BUCKET_NAME_LENGTH = 3;
    private static final int MAX_BUCKET_NAME_LENGTH = 63;

    /**
     * Validates that none of the provided "required" parameters are null or empty.
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
     */
    public static boolean isNullOrEmpty(String path) {
        return (path == null || path.trim().isEmpty());
    }

    /**
     * Validates a bucket or directory name according to the current storage service type.
     */
    public static boolean isValidBucketName(String name) {
        return name.length() >= MIN_BUCKET_NAME_LENGTH && name.length() <= MAX_BUCKET_NAME_LENGTH;
    }

    /**
     * Validates the directory path by checking each segment.
     */
    public static boolean isValidPath(String[] parts) {
        return Arrays.stream(parts)
                .allMatch(dir -> !dir.isEmpty() && isValidBucketName(dir));
    }
}
