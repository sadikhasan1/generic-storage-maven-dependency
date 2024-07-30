package com.dsi.storage.core;

import java.io.InputStream;

public interface StorageService {

    /**
     * Uploads an object to the storage service.
     *
     * @param objectName  the name of the object to be uploaded
     * @param data        the input stream of the data to be uploaded
     * @param contentType the content type of the data
     * @throws Exception if any error occurs during the upload
     */
    void upload(String bucketName, String objectName, InputStream data, String contentType) throws Exception;

    /**
     * Downloads an object from the storage service.
     *
     * @param objectName the name of the object to be downloaded
     * @return an input stream of the downloaded data
     * @throws Exception if any error occurs during the download
     */
    InputStream download(String bucketName, String objectName) throws Exception;

    /**
     * Deletes an object from the storage service.
     *
     * @param objectName the name of the object to be deleted
     * @throws Exception if any error occurs during the deletion
     */
    void delete(String bucketName, String objectName) throws Exception;
}