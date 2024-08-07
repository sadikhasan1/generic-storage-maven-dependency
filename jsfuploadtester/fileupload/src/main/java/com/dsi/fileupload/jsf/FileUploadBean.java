package com.dsi.fileupload.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import com.dsi.storage.core.StorageService;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

@Named
@ViewScoped
public class FileUploadBean implements Serializable {
    private UploadedFiles files;

    public UploadedFiles getFiles() {
        return files;
    }

    public void setFiles(UploadedFiles files) {
        this.files = files;
    }

    public void upload() {
        System.out.println("start");
        if (files != null && !files.getFiles().isEmpty()) {
            for (UploadedFile file : files.getFiles()) {
                System.out.println("Processing file: " + file.getFileName());
                try (InputStream inputStream = file.getInputStream()) {
                    StorageService.upload("random", file.getFileName(), inputStream, file.getContentType());
                } catch (IOException e) {
                    System.err.println("Error uploading file: " + file.getFileName());
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("No file selected.");
        }
    }
}