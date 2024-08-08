# Storage Integration Library

This guide will help you implement the `storage-integration` library for file upload and download functionality using JSF and Spring Boot.

## Maven Dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.dsi</groupId>
    <artifactId>storage-integration</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>

<repositories>
    <repository>
        <id>nexus-releases</id>
        <url>https://sonatype.innovatorslab.net/repository/maven-public/</url>
    </repository>
</repositories>
```

Add the following lines to your `~/.m2/settings.xml`:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">
    <servers>
        <server>
            <id>nexus-releases</id>
	    <username>admin</username>
	    <password>pass</password>
        </server>
        <server>
            <id>nexus-snapshots</id>
	    <username>admin</username>
	    <password>pass</password>
        </server>
    </servers>
</settings>
```

## Gradle Dependency

Add the following repository and dependency to your `build.gradle`:

```gradle
repositories {
    maven {
        url 'https://sonatype.innovatorslab.net/repository/maven-public/'
        credentials {
            username = 'admin'
            password = 'pass'
        }
    }
    mavenCentral()
}

dependencies {
    implementation 'com.dsi:storage-integration:1.0-SNAPSHOT'
}
```

## Environment Variables

### Bash

Add the following environment variables to your `.bashrc` or `.bash_profile`:

```bash
export STORAGE_SERVICE_TYPE=minio
export MINIO_ENDPOINT=https://your-minio-endpoint
export MINIO_ACCESS_KEY=your-access-key
export MINIO_SECRET_KEY=your-secret-key
```

Reload the configuration:

```bash
source ~/.bashrc
```

### IntelliJ IDEA

To set environment variables in IntelliJ IDEA:

1. Open `Run/Debug Configurations`.
2. Select your configuration.
3. Add the environment variables under the `Environment variables` section.

## JSF Implementation

### FileUploadBean.java

```java
package com.dsi.fileupload.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import com.dsi.storage.core.StorageService;
import com.dsi.storage.core.StorageServiceFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class FileUploadBean implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(FileUploadBean.class.getName());
    private final StorageService storageService;
    private UploadedFiles files;

    public FileUploadBean() {
        this.storageService = StorageServiceFactory.getStorageService();
    }

    public UploadedFiles getFiles() {
        return files;
    }

    public void setFiles(UploadedFiles files) {
        this.files = files;
    }

    public void upload() {
        LOGGER.log(Level.INFO, "Upload started");
        if (files != null && !files.getFiles().isEmpty()) {
            for (UploadedFile file : files.getFiles()) {
                try (InputStream inputStream = file.getInputStream()) {
                    storageService.upload("jsf/test/for/nested", file.getFileName(), inputStream, file.getContentType());
                    LOGGER.log(Level.INFO, "Uploaded file: {0}", file.getFileName());
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error uploading file: " + file.getFileName(), e);
                }
            }
        } else {
            LOGGER.log(Level.WARNING, "No file selected.");
        }
        LOGGER.log(Level.INFO, "Upload finished");
    }
}
```

### index.xhtml

```xml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:p="http://primefaces.org/ui">

<h:head>
    <title>File Upload Example</title>
</h:head>
<h:body>
    <h:form enctype="multipart/form-data">
        <p:fileUpload value="#{fileUploadBean.files}"
                      mode="advanced"
                      multiple="true"
                      auto="true"
                      update="messages" />
        <p:commandButton value="Upload"
                         action="#{fileUploadBean.upload}"
                         update="messages" />
        <p:messages id="messages" />
    </h:form>
</h:body>
</html>
```

## Spring Boot Implementation

### TestController.java

```java
package com.dsi.fileupload;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.dsi.storage.core.StorageService;
import com.dsi.storage.core.StorageServiceFactory;

import java.io.IOException;

@Controller
public class TestController {
    private final StorageService storageService;

    public TestController() {
        this.storageService = StorageServiceFactory.getStorageService();
    }

    @GetMapping("/")
    public String index(@RequestParam(defaultValue = "") String filePath, Model model) {
        model.addAttribute("filePath", filePath);
        return "test";
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String filePath) throws Exception {
        return StorageService.downloadAsResponseEntityForResource(filePath);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {
        String bucket = "just/atest/for/nested";
        String filePath = storageService.upload(bucket, file.getOriginalFilename(), file.getInputStream(), file.getContentType());
        redirectAttributes.addAttribute("filePath", filePath);
        return "redirect:/";
    }
}
```

### test.html

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>File Upload</title>
</head>
<body>
<form th:action="@{/}" method="post" enctype="multipart/form-data">
    <input type="file" name="file" />
    <input type="submit" value="Upload" />
    <a th:if="${filePath != ''}" th:href="@{/download(filePath=${filePath})}">Download</a>
</form>
</body>
</html>
```

By following these steps, you will have integrated the `storage-integration` library into your JSF and Spring Boot applications. This will allow you to handle file uploads and downloads seamlessly with various storage backends based on environment configurations.
