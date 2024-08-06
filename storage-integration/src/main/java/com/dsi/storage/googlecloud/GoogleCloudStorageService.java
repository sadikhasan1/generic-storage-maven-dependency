package com.dsi.storage.googlecloud;

import com.dsi.storage.core.StorageService;
import com.dsi.storage.dto.BucketObject;
import com.dsi.storage.util.FileUtils;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.primefaces.model.file.UploadedFile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;

public class GoogleCloudStorageService extends StorageService {

    private final Storage storage;

    public GoogleCloudStorageService(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String upload(String bucketName, String objectName, InputStream data, String contentType) {
        String filename = FileUtils.appendUUIDToFilename(objectName);
        BlobId blobId = BlobId.of(bucketName, filename);
        BlobInfo blobInfo  = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();
        storage.create(blobInfo, data);
        return filename;
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
        Blob blob = storage.get(BlobId.of(bucketName, objectName));
        if (blob == null) {
            return null;
        }
        ReadChannel readChannel = blob.reader();
        return Channels.newInputStream(readChannel);
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