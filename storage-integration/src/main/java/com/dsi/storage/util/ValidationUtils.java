package com.dsi.storage.util;

/**
 * Provides utility methods for validating input parameters.
 */
public class ValidationUtils {

    /**
     * Validates that none of the provided "required" parameters are null or empty.
     * @param params The parameters to validate.
     * @throws IllegalArgumentException If any of the parameters are null or empty.
     */
    public static void emptyCheckOnRequiredFields(String... params) throws IllegalArgumentException {
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
