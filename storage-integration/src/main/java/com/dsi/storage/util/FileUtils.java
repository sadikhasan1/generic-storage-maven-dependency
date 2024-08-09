package com.dsi.storage.util;

import com.dsi.storage.dto.BucketObject;
import org.apache.tika.Tika;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class FileUtils {

    private static final Tika tika = new Tika();

    public static String detectMimeType(byte[] fileData) {
        return tika.detect(fileData);
    }

    public static byte[] readInputStreamToByteArray(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }


    public static String getBaseBucketName(String bucketName) {
        String[] parts = bucketName.split("/");
        return parts[0];
    }

    public static String appendUUIDToFilename(String bucketName, String objectName) {
        // Generate a unique identifier
        String uuid = UUID.randomUUID().toString();

        // Extract the part of the bucketName after the first slash if it exists
        String bucketPrefix = bucketName.contains("/")
                ? bucketName.substring(bucketName.indexOf('/') + 1)
                : "";

        // Process objectName to handle extension
        int dotIndex = objectName.lastIndexOf('.');
        String baseName = dotIndex == -1 ? objectName : objectName.substring(0, dotIndex);
        String extension = dotIndex == -1 ? "" : objectName.substring(dotIndex);

        // Construct new file name with UUID and optional bucketPrefix
        String newFileName;
        if (bucketPrefix.isEmpty()) {
            newFileName = baseName + '-' + uuid + extension;
        } else {
            newFileName = bucketPrefix + "/" + baseName + '-' + uuid + extension;
        }

        // Normalize the file path to remove double slashes and ensure single slashes
        newFileName = newFileName.replaceAll("/+", "/");

        // Ensure the file name does not start with a slash
        if (newFileName.startsWith("/")) {
            newFileName = newFileName.substring(1);
        }

        return newFileName;
    }

    public static String appendUUIDToFilename(String objectName) {
        String uuid = UUID.randomUUID().toString();
        int dotIndex = objectName.lastIndexOf('.');
        if (dotIndex == -1) {
            return objectName + '-' + uuid;
        }
        String baseName = objectName.substring(0, dotIndex);
        String extension = objectName.substring(dotIndex);
        return baseName + '-' + uuid + extension;
    }

    public static BucketObject extractBucketAndObjectName(String filePath) {
        int firstSlashIndex = filePath.indexOf('/');

        // Check if "/" exists in the filePath
        if (firstSlashIndex != -1) {
            // Extract bucketName and objectName
            String bucketName = filePath.substring(0, firstSlashIndex);
            String objectName = filePath.substring(firstSlashIndex + 1);

            return new BucketObject(bucketName, objectName);
        } else {
            // Handle case where "/" is not found (return null or handle differently)
            return null;
        }
    }

    public static String getOriginalFileName(String objectName) {
        int lastSlashIndex = objectName.lastIndexOf('/');

        // Check if "/" exists in the filePath
        if (lastSlashIndex != -1) {
            // Extract and return the file name
            return objectName.substring(lastSlashIndex + 1);
        } else {
            // Handle case where "/" is not found (return the whole string or handle differently)
            return objectName;
        }
    }

    public static void validateNotEmpty(String... params) {
        StringBuilder errorMessage = new StringBuilder("The following parameters are invalid: ");
        boolean hasInvalidParams = false;

        for (int i = 0; i < params.length; i++) {
            if (params[i] == null || params[i].isEmpty()) {
                if (hasInvalidParams) {
                    errorMessage.append(", ");
                }
                errorMessage.append("Parameter ").append(i + 1);
                hasInvalidParams = true;
            }
        }

        if (hasInvalidParams) {
            throw new IllegalArgumentException(errorMessage.toString());
        }
    }

}
