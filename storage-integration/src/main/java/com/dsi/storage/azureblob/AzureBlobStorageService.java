package com.dsi.storage.azureblob;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.dsi.storage.core.StorageService;

import java.io.InputStream;

public class AzureBlobStorageService implements StorageService {

    private final BlobContainerClient blobContainerClient;

    public AzureBlobStorageService(String connectionString, String containerName) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        this.blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
    }

    @Override
    public void upload(String objectName, InputStream data, String contentType) throws Exception {
        BlobClient blobClient = blobContainerClient.getBlobClient(objectName);
        blobClient.upload(data, data.available(), true);
    }

    @Override
    public InputStream download(String objectName) throws Exception {
        BlobClient blobClient = blobContainerClient.getBlobClient(objectName);
        return blobClient.openInputStream();
    }

    @Override
    public void delete(String objectName) throws Exception {
        BlobClient blobClient = blobContainerClient.getBlobClient(objectName);
        blobClient.delete();
    }
}