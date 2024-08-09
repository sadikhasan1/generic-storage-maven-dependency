package com.dsi.storage.client;

import java.io.InputStream;

public interface StorageClient {
    String upload(String bucketName, String objectName, InputStream data, String contentType);
    InputStream download(String bucketName, String objectName);
    InputStream download(String filePath);
}
