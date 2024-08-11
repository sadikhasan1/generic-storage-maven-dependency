package com.dsi.storage.minio;

import com.dsi.storage.exception.StorageException;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MinioExceptions {
    private static final Logger logger = LoggerFactory.getLogger(MinioUtils.class);

    static StorageException handleMinioException(String operation, MinioException e) {
        logger.error("MinIO error while {}: {}", operation, e.getMessage());
        return new StorageException("MinIO error while " + operation, e);
    }

    static StorageException handleIOException(String operation, IOException e) {
        logger.error("IO error while {}: {}", operation, e.getMessage());
        return new StorageException("IO error during " + operation, e);
    }

    static StorageException handleUnexpectedException(String operation, Exception e) {
        logger.error("Unexpected error while {}: {}", operation, e.getMessage());
        return new StorageException("Unexpected error during " + operation, e);
    }
}
