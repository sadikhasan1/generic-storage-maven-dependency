package com.dsi.storage.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.dsi.storage.core.StorageService;
import java.io.InputStream;

public class S3StorageService implements StorageService {

    private final AmazonS3 amazonS3;
    private final String bucketName;

    public S3StorageService(AmazonS3 amazonS3, String bucketName) {
        this.amazonS3 = amazonS3;
        this.bucketName = bucketName;
    }

    @Override
    public void upload(String objectName, InputStream data, String contentType) throws Exception {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        amazonS3.putObject(new PutObjectRequest(bucketName, objectName, data, metadata));
    }

    @Override
    public InputStream download(String objectName) throws Exception {
        S3Object s3Object = amazonS3.getObject(bucketName, objectName);
        return s3Object.getObjectContent();
    }

    @Override
    public void delete(String objectName) throws Exception {
        amazonS3.deleteObject(bucketName, objectName);
    }
}