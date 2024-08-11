package com.dsi.storage.minio;

import com.dsi.storage.dto.BucketObject;
import com.dsi.storage.exception.StorageException;
import io.minio.*;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MinioUtils {
    private static final Logger logger = LoggerFactory.getLogger(MinioUtils.class);

    static void ensureBucketExists(MinioClient minioClient, String baseBucketName) throws StorageException {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(baseBucketName).build());
            if (!bucketExists) {
                // Create the bucket if it doesn't exist
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(baseBucketName).build());
                logger.info("Bucket '{}' created successfully.", baseBucketName);
            }
        } catch (MinioException e) {
            throw MinioExceptions.handleMinioException("ensureBucketExists", e);
        } catch (InvalidKeyException e) {
            logger.error("Invalid key while ensuring bucket existence: {}", e.getMessage());
            throw new StorageException("Invalid key while ensuring bucket existence", e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("No such algorithm while ensuring bucket existence: {}", e.getMessage());
            throw new StorageException("No such algorithm while ensuring bucket existence", e);
        } catch (IOException e) {
            logger.error("IO error while ensuring bucket existence: {}", e.getMessage());
            throw new StorageException("IO error while ensuring bucket existence", e);
        } catch (Exception e) {
            throw MinioExceptions.handleUnexpectedException("ensureBucketExists", e);
        }
    }

    static String getContentType(MinioClient minioClient, BucketObject bucketObject) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        StatObjectResponse statObject = minioClient.statObject(StatObjectArgs.builder()
                .bucket(bucketObject.bucketName())
                .object(bucketObject.objectName())
                .build()
        );
        return statObject.contentType();
    }

    static GetObjectResponse getObjectResponse(MinioClient minioClient, BucketObject bucketObject) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketObject.bucketName())
                .object(bucketObject.objectName())
                .build()
        );
    }

}
