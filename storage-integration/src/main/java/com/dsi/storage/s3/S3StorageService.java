package com.dsi.storage.s3;


import com.dsi.storage.dto.BucketObject;
import com.dsi.storage.util.FileUtils;
import org.primefaces.model.file.UploadedFile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import com.dsi.storage.core.StorageService;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.io.InputStream;


public class S3StorageService extends StorageService {

    private final S3Client s3Client;

    public S3StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String upload(String bucketName, String objectName, InputStream data, String contentType) {
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

    @Override
    public String upload(String containerName, UploadedFile uploadedFile) {
        try {
            return upload(containerName, uploadedFile.getFileName(), uploadedFile.getInputStream(), uploadedFile.getContentType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String upload(String containerName, MultipartFile file) {
        try {
            return upload(containerName, file.getOriginalFilename(), file.getInputStream(), file.getContentType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream download(String bucketName, String objectName) {
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .build());
        } catch (S3Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream download(String filePath) {
        BucketObject bucketObject = FileUtils.extractBucketAndObjectName(filePath);
        return download(bucketObject.getBucketName(),  bucketObject.getObjectName());
    }

    @Override
    public InputStreamResource downloadInputStreamResource(String filePath) {
        try {
            BucketObject bucketObject = FileUtils.extractBucketAndObjectName(filePath);
            InputStream inputStream = download(filePath);
            return FileUtils.convertToInputStreamResource(inputStream, bucketObject.getObjectName());
        } catch (Exception e) {
            throw new RuntimeException("Error converting to UploadedFile", e);
        }
    }

    @Override
    public UploadedFile downloadAsUploadedFile(String filePath) {
        try {
            BucketObject bucketObject = FileUtils.extractBucketAndObjectName(filePath);
            InputStream inputStream = download(filePath);
            return FileUtils.convertToUploadedFile(inputStream, bucketObject.getObjectName());
        } catch (Exception e) {
            throw new RuntimeException("Error converting to UploadedFile", e);
        }
    }

    @Override
    public MultipartFile downloadAsMultipartFile(String filePath) {
        try {
            BucketObject bucketObject = FileUtils.extractBucketAndObjectName(filePath);
            InputStream inputStream = download(filePath);
            return FileUtils.convertToMultipartFile(inputStream, bucketObject.getObjectName());
        } catch (Exception e) {
            throw new RuntimeException("Error converting to MultipartFile", e);
        }
    }

    @Override
    public ResponseEntity<Resource> downloadAsResponseEntityForResource(String filePath) {
        try {
            BucketObject bucketObject = FileUtils.extractBucketAndObjectName(filePath);
            InputStream inputStream = download(filePath);
            byte[] fileData = FileUtils.readInputStreamToByteArray(inputStream);
            String contentType = FileUtils.detectMimeType(fileData);
            return FileUtils.createResponseEntityForResource(fileData, bucketObject.getObjectName(), contentType);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to MultipartFile", e);
        }
    }

    @Override
    public ResponseEntity<InputStreamResource> downloadAsResponseEntityForInputStreamResource(String filePath) {
        try {
            BucketObject bucketObject = FileUtils.extractBucketAndObjectName(filePath);
            InputStream inputStream = download(filePath);
            byte[] fileData = FileUtils.readInputStreamToByteArray(inputStream);
            String contentType = FileUtils.detectMimeType(fileData);
            return FileUtils.createResponseEntityForInputStreamResource(fileData, bucketObject.getObjectName(), contentType);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to MultipartFile", e);
        }
    }
}