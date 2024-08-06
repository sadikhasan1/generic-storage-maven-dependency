package com.dsi.storage.minio;

import com.dsi.storage.core.StorageService;
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
            String filename = FileUtils.appendUUIDToFilename(objectName);
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .stream(data, -1, 10485760) // -1 for unknown size, 10485760 for max size (10MB)
                            .contentType(contentType)
                            .build()
            );
            return filename;
        } catch (Exception e) {
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