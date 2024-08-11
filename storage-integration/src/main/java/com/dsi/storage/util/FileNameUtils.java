package com.dsi.storage.util;

import java.util.UUID;

/**
 * Provides utility methods related to file names and paths.
 */
public class FileNameUtils {

    /**
     * Generates a unique file ID and appends it to the nested folder path.
     * @param fullPath The full path including bucket and nested folders.
     * @return The new file name with the unique ID.
     * @throws IllegalArgumentException If the fullPath is null or empty.
     */
    public static String generateUniqueFileIdWithNestedFolders(String fullPath) throws IllegalArgumentException {
        if (fullPath == null || fullPath.trim().isEmpty()) {
            throw new IllegalArgumentException("The fullPath cannot be null or empty.");
        }

        String uniqueId = UUID.randomUUID().toString();
        String nestedFolders = fullPath.contains("/")
                ? fullPath.substring(fullPath.indexOf('/') + 1)
                : "";

        String fileName = nestedFolders.isEmpty()
                ? uniqueId
                : nestedFolders + "/" + uniqueId;

        return fileName.replaceAll("/+", "/").replaceFirst("^/", "");
    }
}
