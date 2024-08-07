package com.dsi.storage.s3;

import com.dsi.storage.dto.BucketObject;
import com.dsi.storage.util.FileUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;


public class S3StorageService {
    public static String upload(String bucketName, String objectName, InputStream data, String contentType) {
        S3Client s3Client = getS3Client();
        try {
            String filename = FileUtils.appendUUIDToFilename(objectName);
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(data, data.available()));
            return filename;
        } catch (S3Exception | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static S3Client getS3Client() {
        String endpoint = System.getenv("STORAGE_ENDPOINT");
        String accessKey = System.getenv("STORAGE_ACCESS_KEY");
        String secretKey = System.getenv("STORAGE_SECRET_KEY");
        String region = System.getenv("STORAGE_REGION");
        FileUtils.validateNotEmpty(endpoint, accessKey, secretKey, region);

        S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .build();
        return s3Client;
    }

    public static InputStream download(String bucketName, String objectName) {
        S3Client s3Client = getS3Client();
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .build());
        } catch (S3Exception e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream download(String filePath) {
        BucketObject bucketObject = FileUtils.extractBucketAndObjectName(filePath);
        return download(bucketObject.getBucketName(),  bucketObject.getObjectName());
    }
}