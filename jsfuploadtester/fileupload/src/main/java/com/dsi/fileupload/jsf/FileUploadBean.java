package com.dsi.fileupload.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import com.dsi.storage.core.StorageService;

import java.io.Serializable;

@Named
@ViewScoped
public class FileUploadBean implements Serializable {

    private final StorageService storageService;

    public FileUploadBean() {
        this.storageService = StorageService.init();
    }

    private UploadedFiles files;

    public UploadedFiles getFiles() {
        return files;
    }

    public void setFiles(UploadedFiles files) {
        this.files = files;
    }

    public void upload() {
        if (files != null && !files.getFiles().isEmpty()) {
            for (UploadedFile file : files.getFiles()) {
                storageService.upload("jsf/testing/nested/folder", file);
            }
        } else {
            System.out.println("No file selected.");
        }
        System.out.println("No file selected.");
    }
}
