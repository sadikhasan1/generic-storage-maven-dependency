package com.dsi.storage.azureblob;

import com.azure.storage.blob.*;
import com.dsi.storage.core.StorageService;

import java.io.InputStream;

public class AzureBlobStorageService implements StorageService {

    private final BlobContainerClient containerClient;

    public AzureBlobStorageService(String connectionString) {
        this.containerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName("container-name") // You need to specify the container name dynamically or provide a method to set it
                .buildClient();
    }

    @Override
    public void upload(String bucketName, String objectName, InputStream data, String contentType) throws Exception {
        BlobClient blobClient = containerClient.getBlobClient(objectName);
        blobClient.upload(data, data.available(), true);
    }

    @Override
    public InputStream download(String bucketName, String objectName) throws Exception {
        BlobClient blobClient = containerClient.getBlobClient(objectName);
        return blobClient.openInputStream();
    }

    @Override
    public void delete(String bucketName, String objectName) throws Exception {
        BlobClient blobClient = containerClient.getBlobClient(objectName);
        blobClient.delete();
    }
}