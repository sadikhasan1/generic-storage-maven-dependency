package com.dsi.storage.minio;

import com.dsi.storage.core.StorageService;
import com.dsi.storage.dto.BucketObject;
import com.dsi.storage.util.FileUtils;
import io.minio.*;
import io.minio.errors.*;
import org.apache.tika.Tika;
import org.jetbrains.annotations.NotNull;
import org.primefaces.model.file.UploadedFile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MinioStorageService extends StorageService {

    private final MinioClient minioClient;


    public MinioStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public String upload(String bucketName, String objectName, InputStream data, String contentType) {
        try {
            String filename = FileUtils.appendUUIDToFilename(bucketName, objectName);
            String baseBucketName = FileUtils.getBaseBucketName(bucketName);
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

    @Override
    public String upload(String bucketName, UploadedFile uploadedFile) {
        try {
            return upload(bucketName, uploadedFile.getFileName(), uploadedFile.getInputStream(), uploadedFile.getContentType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String upload(String bucketName, MultipartFile file) {
        try {
            return upload(bucketName, file.getOriginalFilename(), file.getInputStream(), file.getContentType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream download(String filePath) {
        BucketObject bucketObject = FileUtils.extractBucketAndObjectName(filePath);
        return download(bucketObject.getBucketName(),  bucketObject.getObjectName());
    }

    @Override
    public InputStream download(String bucketName, String objectName) {
        try {
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