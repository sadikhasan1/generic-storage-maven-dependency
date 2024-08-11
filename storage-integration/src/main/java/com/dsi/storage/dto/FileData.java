package com.dsi.storage.dto;

import java.io.InputStream;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record FileData(InputStream inputStream, String contentType) {
    private static final Tika tika = new Tika();
    private static final Logger logger = LoggerFactory.getLogger(FileData.class);

    /**
     * Determines the file extension based on the content type using Apache Tika.
     * If the content type is null, empty, or invalid, an empty string is returned.
     *
     * @return The file extension including the leading dot (e.g., ".pdf"), or an empty string if the extension could not be determined.
     */
    public String fileExtension() {
        if (contentType == null || contentType.trim().isEmpty()) {
            logger.warn("Content type is null or empty.");
            return "";
        }

        try {
            // Use Tika's MimeTypes class to get the extension from the content type
            MimeTypes mimeTypes = MimeTypes.getDefaultMimeTypes();
            MediaType mediaType = MediaType.parse(contentType);
            String fileExtension = mimeTypes.forName(mediaType.toString()).getExtension();

            return fileExtension != null ? fileExtension : "";
        } catch (Exception e) {
            // Log the error with appropriate level
            logger.error("Failed to determine file extension for content type '{}': {}", contentType, e.getMessage());
            return "";
        }
    }

}
