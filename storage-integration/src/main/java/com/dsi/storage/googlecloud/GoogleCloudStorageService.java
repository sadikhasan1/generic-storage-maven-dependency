package com.dsi.storage.googlecloud;

import com.dsi.storage.client.StorageClient;
import com.dsi.storage.dto.BucketObject;
import com.dsi.storage.util.FileUtils;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.*;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.List;

public class GoogleCloudStorageService implements StorageClient {
    private final Storage storage;

    public GoogleCloudStorageService() {
        String projectId = System.getenv("STORAGE_PROJECT_ID");
        String credentialsFilePath = System.getenv("STORAGE_CREDENTIALS_FILE_PATH");
        FileUtils.validateNotEmpty(projectId, credentialsFilePath);

        GoogleCredentials credentials = null;
        try {
            credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsFilePath))
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build()
                .getService();
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
}