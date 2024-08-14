package com.dsi.storage.util;

import com.dsi.storage.exception.StorageException;

public class PathUtil {

    /**
     * Splits a given path into segments for upload operations.
     * It normalizes the path by replacing multiple slashes with a single slash
     * and removing leading and trailing slashes.
     * Additionally, it validates the path segments to ensure they meet naming rules.
     */
    public static String[] splitPathForUpload(String fullPath) throws StorageException {
        fullPath = normalize(fullPath);

        // Split the normalized path by '/'
        String[] parts = fullPath.split("/");

        // Trim leading and trailing spaces from each part
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim(); // Remove leading and trailing spaces
        }

        // Validate each segment of the path
        if (!ValidationUtils.isValidPath(parts)) {
            throw new StorageException("Invalid directory path: " + fullPath);
        }

        return parts;
    }

    private static String normalize(String fullPath) {
        // Normalize the path
        fullPath = fullPath.trim().replaceAll("/+", "/")  // Replace multiple slashes with a single slash
                .replaceAll("^/|/$", ""); // Remove leading and trailing slashes
        return fullPath;
    }

    /**
     * Splits a given path into segments for download operations.
     * It normalizes the path by replacing multiple slashes with a single slash,
     * and removing leading and trailing slashes.
     */
    public static String[] splitPathForDownload(String fullPathWithFileId) {
        // Normalize the path
        fullPathWithFileId = normalize(fullPathWithFileId);

        // Split the normalized path by '/'
        return fullPathWithFileId.split("/");
    }
}
