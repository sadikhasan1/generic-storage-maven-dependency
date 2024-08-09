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
export STORAGE_ENDPOINT=https://your-minio-endpoint
export STORAGE_ACCESS_KEY=your-access-key
export STORAGE_SECRET_KEY=your-secret-key
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

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.model.file.UploadedFile;
import com.dsi.storage.core.StorageService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;

@Named
@ViewScoped
public class FileUploadBean implements Serializable {
    private final StorageService storageService = new StorageService();
    private UploadedFile file;
    private String filepath;  // Field to store the uploaded file path

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public String getFilepath() {
        return filepath;
    }

    public void upload() {
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                filepath = storageService.upload("random/for/test", file.getFileName(), inputStream, file.getContentType());
            } catch (IOException e) {
                System.err.println("Error uploading file: " + file.getFileName());
                e.printStackTrace();
            }
        } else {
            System.out.println("No file selected.");
        }
    }

    public void download() {
        if (filepath != null) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();
            externalContext.responseReset();

            try (InputStream inputStream = storageService.download(filepath);
                 OutputStream outputStream = externalContext.getResponseOutputStream()) {

                externalContext.setResponseContentType(Files.probeContentType(Paths.get(filepath)));
                externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\"" + Paths.get(filepath).getFileName().toString() + "\"");

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
                facesContext.responseComplete();

            } catch (IOException e) {
                System.err.println("Error downloading file: " + filepath);
                e.printStackTrace();
            }
        }
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
        <p:fileUpload value="#{fileUploadBean.file}"
                      mode="advanced"
                      multiple="false"
                      auto="true"
                      update="messages" />
        <p:commandButton value="Upload"
                         action="#{fileUploadBean.upload}"
                         update="messages, downloadButton" />
        <p:messages id="messages" />
        <h:panelGroup id="downloadButton">
            <h:commandButton value="Download"
                             action="#{fileUploadBean.download}"
                             rendered="#{not empty fileUploadBean.filepath}"
                             immediate="true"
                             disableClientWindow="true" />
        </h:panelGroup>
    </h:form>
</h:body>
</html>
```

## Spring Boot Implementation

### TestController.java

```java
package com.example.fileuploadtester.test;

import com.dsi.storage.core.StorageService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayOutputStream;


@Controller
public class TestController {
    private final StorageService storageService = new StorageService();
    
    @GetMapping("/")
    public String index(
            @RequestParam(defaultValue = "") String filePath,
            Model model) {
        model.addAttribute("filePath", filePath);
        return "test";
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String filePath) throws Exception {
        InputStream inputStream = storageService.download(filePath);
        if (inputStream == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        byte[] bytes = inputStream.readAllBytes();
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(bytes));
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(bytes.length)
                .body(resource);
    }

    @GetMapping("/image-manual-response")
    public void getImageAsByteArray(HttpServletResponse response) throws IOException {
        InputStream in = storageService.download("bucketname/nested/folder/image.png");
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        IOUtils.copy(in, response.getOutputStream());
    }


    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {
        String bucket = "test";
        return "redirect:/?filePath=" + storageService.upload(bucket, file.getOriginalFilename(), file.getInputStream(), file.getContentType());
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
    <a th:if="${filePath != ''}"  th:href="@{/download(filePath=${filePath})}">Download</a>

</form>
</body>
</html>
```

By following these steps, you will have integrated the `storage-integration` library into your JSF and Spring Boot applications. This will allow you to handle file uploads and downloads seamlessly with various storage backends based on environment configurations.
