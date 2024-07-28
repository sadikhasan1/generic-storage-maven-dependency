package com.dsi.storage.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class StorageProperties {
    private String service;
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    public StorageProperties() {
        loadProperties();
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                System.out.println("Sorry, unable to find application.properties");
                return;
            }
            prop.load(input);
            this.service = prop.getProperty("storage.service");
            this.endpoint = prop.getProperty("storage.endpoint");
            this.accessKey = prop.getProperty("storage.accessKey");
            this.secretKey = prop.getProperty("storage.secretKey");
            this.bucketName = prop.getProperty("storage.bucketName");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Getters and Setters
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public String getBucketName() { return bucketName; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }
}