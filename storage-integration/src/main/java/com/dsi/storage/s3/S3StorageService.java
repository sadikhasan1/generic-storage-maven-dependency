package com.dsi.storage.s3;


import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import com.dsi.storage.core.StorageService;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;


public class S3StorageService implements StorageService {

    private final S3Client s3Client;

    public S3StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public void upload(String bucketName, String objectName, InputStream data, String contentType) throws Exception {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(data, data.available()));
        } catch (S3Exception e) {
            throw new Exception("Error uploading object to S3: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream download(String bucketName, String objectName) throws Exception {
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .build());
        } catch (S3Exception e) {
            throw new Exception("Error downloading object from S3: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String bucketName, String objectName) throws Exception {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .build());
        } catch (S3Exception e) {
            throw new Exception("Error deleting object from S3: " + e.getMessage(), e);
        }
    }
}