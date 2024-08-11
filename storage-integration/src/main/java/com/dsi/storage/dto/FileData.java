package com.dsi.storage.dto;

import java.io.InputStream;

public record FileData(InputStream inputStream, String contentType) {
}
