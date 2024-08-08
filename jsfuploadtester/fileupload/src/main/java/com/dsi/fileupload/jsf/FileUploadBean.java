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
    private UploadedFile file;

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {  // Corrected setter method name
        this.file = file;
    }

    public void upload() {
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                StorageService.upload("jsf/test/for/nested", file.getFileName(), inputStream, file.getContentType());
            } catch (IOException e) {
                System.err.println("Error uploading file: " + file.getFileName());
                e.printStackTrace();
            }
        } else {
            System.out.println("No file selected.");
        }
    }
}