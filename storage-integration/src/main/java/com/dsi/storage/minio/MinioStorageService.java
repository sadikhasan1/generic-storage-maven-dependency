package com.dsi.storage.minio;

import com.dsi.storage.dto.BucketObject;
import com.dsi.storage.util.FileUtils;
import io.minio.*;
import java.io.*;

public class MinioStorageService {

    private static MinioClient getMinioClient() {
        String endpoint = System.getenv("STORAGE_ENDPOINT");
        String accessKey = System.getenv("STORAGE_ACCESS_KEY");
        String secretKey = System.getenv("STORAGE_SECRET_KEY");
        FileUtils.validateNotEmpty(endpoint, accessKey, secretKey);
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    public static String upload(String bucketName, String objectName, InputStream data, String contentType) {
        try {
            MinioClient minioClient = getMinioClient();
            String filename = FileUtils.appendUUIDToFilename(bucketName, objectName);
            String baseBucketName = FileUtils.getBaseBucketName(bucketName);
            System.out.println("FileName = " + filename + ", baseBucketName = " + baseBucketName);
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(baseBucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(baseBucketName).build());
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(baseBucketName)
                            .object(filename)
                            .stream(data, -1, 10485760) // -1 for unknown size, 10485760 for max size (10MB)
                            .contentType(contentType)
                            .build()
            );
            return baseBucketName + "/" + filename;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream download(String bucketName, String objectName) {
        try {
            MinioClient minioClient = getMinioClient();
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file", e);
        }
    }

    public InputStream download(String filePath) {
        BucketObject bucketObject = FileUtils.extractBucketAndObjectName(filePath);
        return download(bucketObject.getBucketName(),  bucketObject.getObjectName());
    }
}