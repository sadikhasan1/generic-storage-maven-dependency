package com.dsi.storage.s3;


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
    public String upload(String bucketName, String objectName, UploadedFile uploadedFile) {
        try {
            return upload(bucketName, objectName, uploadedFile.getInputStream(), uploadedFile.getContentType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String upload(String bucketName, String objectName, MultipartFile file) {
        try {
            return upload(bucketName, objectName, file.getInputStream(), file.getContentType());
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
    public InputStreamResource downloadInputStreamResource(String bucketName, String objectName) {
        try (InputStream inputStream = download(bucketName, objectName)) {
            return FileUtils.convertToInputStreamResource(inputStream, objectName);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to UploadedFile", e);
        }
    }

    @Override
    public UploadedFile downloadAsUploadedFile(String bucketName, String objectName) {
        try (InputStream inputStream = download(bucketName, objectName)) {
            return FileUtils.convertToUploadedFile(inputStream, objectName);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to UploadedFile", e);
        }
    }

    @Override
    public MultipartFile downloadAsMultipartFile(String bucketName, String objectName) {
        try (InputStream inputStream = download(bucketName, objectName)) {
            return FileUtils.convertToMultipartFile(inputStream, objectName);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to MultipartFile", e);
        }
    }

    @Override
    public ResponseEntity<InputStreamResource> downloadAsResponseEntityForInputStreamResource(String bucketName, String objectName) {
        try (InputStream inputStream = download(bucketName, objectName)) {
            return FileUtils.convertToResponseEntityForInputStreamResource(inputStream, objectName);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to ResponseEntity<InputStreamResource>", e);
        }
    }

    @Override
    public ResponseEntity<Resource> downloadAsResponseEntityForResource(String bucketName, String objectName) {
        try (InputStream inputStream = download(bucketName, objectName)) {
            return FileUtils.convertToResponseEntityForResource(inputStream, objectName);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to ResponseEntity<Resource>", e);
        }
    }
}