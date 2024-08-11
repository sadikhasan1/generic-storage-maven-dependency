package com.dsi.storage.minio;

import com.dsi.storage.util.ValidationUtils;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinioClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(MinioClientFactory.class);
    private static final String endpoint = System.getenv("STORAGE_ENDPOINT");
    private static final String accessKey = System.getenv("STORAGE_ACCESS_KEY");
    private static final String secretKey = System.getenv("STORAGE_SECRET_KEY");
    private static final String partSize = System.getenv("STORAGE_PART_SIZE");

    private static void validateEnvironmentVariables() {
        try {
            ValidationUtils.validateNotEmpty(endpoint, accessKey, secretKey);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Environment variables for MinIO configuration are missing or invalid", e);
        }
    }

    static MinioClient createMinioClient() {
        validateEnvironmentVariables();
        try {
            return MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing MinIO client", e);
        }
    }

    static long getUploadPartSize() {
        long defaultPartSize = 10485760L; // 10 MB default size

        if (partSize != null) {
            try {
                return Long.parseLong(partSize);
            } catch (NumberFormatException e) {
                logger.warn("Invalid STORAGE_PART_SIZE value, using default.", e);
            }
        }
        return defaultPartSize;
    }
}
