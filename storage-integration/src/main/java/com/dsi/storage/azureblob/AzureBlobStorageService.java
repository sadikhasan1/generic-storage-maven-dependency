package com.dsi.storage.azureblob;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobItem;
import com.dsi.storage.core.StorageService;
import com.dsi.storage.dto.BucketObject;
import com.dsi.storage.util.FileUtils;
import org.primefaces.model.file.UploadedFile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public class AzureBlobStorageService extends StorageService {

    private final BlobServiceClient blobServiceClient;

    public AzureBlobStorageService(String connectionString) {
        this.blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    @Override
    public String upload(String containerName, String objectName, InputStream data, String contentType) {
        String filename = FileUtils.appendUUIDToFilename(objectName);
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(filename);
        try {
            blobClient.upload(data, data.available(), true);
            blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));
            return filename;
        } catch (IOException e) {
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
    public InputStream download(String containerName, String objectName) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(objectName);
        return blobClient.openInputStream();
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
