package com.dsi.storage.dto;

import java.io.InputStream;

/**
 * Represents the data associated with a file.
 * This record encapsulates the input stream of the file's content and its MIME type.
 *
 * @param inputStream The InputStream containing the file's data.
 * @param contentType The MIME type of the file.
 */
public record FileData(InputStream inputStream, String contentType) {
}
