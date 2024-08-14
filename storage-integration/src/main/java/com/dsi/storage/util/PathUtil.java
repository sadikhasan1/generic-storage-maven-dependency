package com.dsi.storage.util;

public class PathUtil {

    /**
     * Splits a given path into segments for upload operations.
     * It normalizes the path by replacing multiple slashes with a single slash,
     * replacing spaces with hyphens, and removing leading and trailing slashes.
     * Additionally, it validates the path segments to ensure they meet naming rules.
     */
    public static String[] splitPathForUpload(String fullPath) {
        // Normalize the path
        fullPath = fullPath.trim().replaceAll("/+", "/")  // Replace multiple slashes with a single slash
                .replaceAll(" ", "-")    // Replace spaces with hyphens
                .replaceAll("^/|/$", ""); // Remove leading and trailing slashes

        // Split the normalized path by '/'
        String[] parts = fullPath.split("/");

        // Trim leading and trailing spaces or hyphens from each part
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].replaceAll("^-+|-+$", ""); // Remove leading and trailing hyphens
        }

        // Validate each segment of the path
        if (!ValidationUtils.isValidPath(parts)) {
            throw new IllegalArgumentException("Invalid directory path: " + fullPath);
        }

        return parts;
    }


    /**
     * Splits a given path into segments for download operations.
     * It normalizes the path by replacing multiple slashes with a single slash,
     * and removing leading and trailing slashes.
     */
    public static String[] splitPathForDownload(String fullPathWithFileId) {
        // Normalize the path
        fullPathWithFileId = fullPathWithFileId.trim().replaceAll("/+", "/")  // Replace multiple slashes with a single slash
                .replaceAll("^/|/$", ""); // Remove leading and trailing slashes

        // Split the normalized path by '/'
        return fullPathWithFileId.split("/");
    }
}
