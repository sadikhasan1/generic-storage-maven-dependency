package com.dsi.fileupload.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.model.file.UploadedFile;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Named
@ViewScoped
public class FileUploadBean implements Serializable {

    private UploadedFile file;


    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public void upload() {
        if (file != null) {
            try {
                FileStorageService fileStorageService = new FileStorageService();
                fileStorageService.upload(file.getFileName(), file.getInputStream(), file.getContentType());
                System.out.println("Uploaded file: " + file.getFileName());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error uploading file: " + e.getMessage());
            }
        } else {
            System.out.println("No file selected.");
        }
    }
}
