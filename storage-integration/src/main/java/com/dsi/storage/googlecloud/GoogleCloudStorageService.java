package com.dsi.storage.googlecloud;

import com.dsi.storage.core.StorageService;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.InputStream;
import java.nio.channels.Channels;

public class GoogleCloudStorageService implements StorageService {

    private final Storage storage;

    public GoogleCloudStorageService(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void upload(String bucketName, String objectName, InputStream data, String contentType) throws Exception {
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo  = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();
        storage.create(blobInfo, data);
    }

    @Override
    public InputStream download(String bucketName, String objectName) throws Exception {
        Blob blob = storage.get(BlobId.of(bucketName, objectName));
        if (blob == null) {
            throw new Exception("Object not found");
        }
        ReadChannel readChannel = blob.reader();
        return Channels.newInputStream(readChannel);
    }

    @Override
    public void delete(String bucketName, String objectName) throws Exception {
        BlobId blobId = BlobId.of(bucketName, objectName);
        storage.delete(blobId);
    }
}