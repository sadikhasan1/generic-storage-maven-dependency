package com.dsi.storage.azureblob;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.dsi.storage.client.StorageClient;
import com.dsi.storage.dto.BucketObject;
import com.dsi.storage.util.FileUtils;

import java.io.IOException;
import java.io.InputStream;

public class AzureBlobStorageService implements StorageClient {
    private final BlobServiceClient blobServiceClient;

    public AzureBlobStorageService() {
        String accountName = System.getenv("STORAGE_ACCOUNT_NAME");
        String accountKey = System.getenv("STORAGE_ACCOUNT_KEY");
        FileUtils.validateNotEmpty(accountName, accountKey);

        String connectionString = String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net", accountName, accountKey);
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
}
