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

import com.dsi.storage.dto.FileData;
import com.dsi.storage.exception.StorageException;
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
                filepath = storageService.upload("random/for/test", inputStream, "image/jpeg");
            } catch (IOException e) {
                System.err.println("Error uploading file: " + file.getFileName());
                e.printStackTrace();
            } catch (StorageException e) {
                throw new RuntimeException(e);
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

            try {
                FileData fileData = storageService.download(filepath);
                InputStream inputStream = fileData.inputStream();

                String fileExtension = fileData.fileExtension();
                String fileName = Paths.get(filepath).getFileName().toString() + fileExtension;

                externalContext.setResponseContentType(fileData.contentType());
                externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

                // Write the file content to the response output stream
                OutputStream outputStream = externalContext.getResponseOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();

                // Complete the response to prevent JSF from continuing to render the view
                facesContext.responseComplete();
            } catch (IOException | StorageException e) {
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
package com.dsi.fileupload.jsf;

import com.dsi.storage.dto.FileData;
import com.dsi.storage.exception.StorageException;
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
                filepath = storageService.upload("random/for/test", inputStream, "image/jpeg");
            } catch (IOException e) {
                System.err.println("Error uploading file: " + file.getFileName());
                e.printStackTrace();
            } catch (StorageException e) {
                throw new RuntimeException(e);
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

            try {
                FileData fileData = storageService.download(filepath);
                InputStream inputStream = fileData.inputStream();

                String fileExtension = fileData.fileExtension();
                String fileName = Paths.get(filepath).getFileName().toString() + fileExtension;

                externalContext.setResponseContentType(fileData.contentType());
                externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

                // Write the file content to the response output stream
                OutputStream outputStream = externalContext.getResponseOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();

                // Complete the response to prevent JSF from continuing to render the view
                facesContext.responseComplete();
            } catch (IOException | StorageException e) {
                System.err.println("Error downloading file: " + filepath);
                e.printStackTrace();
            }
        }
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
