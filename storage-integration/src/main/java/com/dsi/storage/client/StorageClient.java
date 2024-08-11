package com.dsi.storage.client;

import com.dsi.storage.dto.FileData;
import com.dsi.storage.exception.StorageException;

import java.io.InputStream;

public interface StorageClient {
    String upload(String fullPath, InputStream data, String contentType) throws StorageException;
    FileData download(String fullPathWithFileId) throws StorageException;
}
