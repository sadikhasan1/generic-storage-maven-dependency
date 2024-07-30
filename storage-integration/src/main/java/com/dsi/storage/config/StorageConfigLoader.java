package com.dsi.storage.config;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class StorageConfigLoader {
    public static StorageConfig loadConfig(String filePath) throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(filePath)) {
            return yaml.loadAs(inputStream, StorageConfig.class);
        }
    }
}