package com.dsi.storage.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void testEmptyCheckOnRequiredFields_AllFieldsValid() {
        String[] params = {"value1", "value2", "value3"};

        assertDoesNotThrow(() -> ValidationUtils.emptyCheckOnRequiredFields(params));
    }

    @Test
    void testEmptyCheckOnRequiredFields_OneFieldEmpty() {
        String[] params = {"value1", "", "value3"};

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.emptyCheckOnRequiredFields(params);
        });

        assertEquals("The following parameters are invalid: Parameter 2", exception.getMessage());
    }

    @Test
    void testEmptyCheckOnRequiredFields_MultipleFieldsEmpty() {
        String[] params = {"value1", "", null, "value4"};

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.emptyCheckOnRequiredFields(params);
        });

        assertEquals("The following parameters are invalid: Parameter 2, Parameter 3", exception.getMessage());
    }

    @Test
    void testIsNullOrEmpty_ValidString() {
        assertFalse(ValidationUtils.isNullOrEmpty("value"));
    }

    @Test
    void testIsNullOrEmpty_NullString() {
        assertTrue(ValidationUtils.isNullOrEmpty(null));
    }

    @Test
    void testIsNullOrEmpty_EmptyString() {
        assertTrue(ValidationUtils.isNullOrEmpty(""));
    }

    @Test
    void testIsValidBucketName_ValidMinioBucketName() {
        String bucketName = "valid-bucket-name";

        assertTrue(ValidationUtils.isValidBucketName(bucketName));
    }

    @Test
    void testIsValidBucketName_InvalidMinioBucketName() {
        String bucketName = "xn--invalid-bucket";

        assertFalse(ValidationUtils.isValidBucketName(bucketName));
    }

    @Test
    void testIsValidPath_ValidPath() {
        String[] parts = {"my-bucket", "folder1", "folder2"};

        assertTrue(ValidationUtils.isValidPath(parts));
    }

    @Test
    void testIsValidPath_InvalidPath() {
        String[] parts = {"my-bucket", "folder1", "invalid--path"};

        assertFalse(ValidationUtils.isValidPath(parts));
    }
}
