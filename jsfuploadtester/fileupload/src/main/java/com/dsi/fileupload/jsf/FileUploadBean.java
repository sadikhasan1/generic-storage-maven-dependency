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
        System.out.println("start");
        if (file != null) {
            System.out.println("Processing file: " + file.getFileName());
            try (InputStream inputStream = file.getInputStream()) {
                filepath = StorageService.upload("random/for/test", file.getFileName(), inputStream, file.getContentType());
                System.out.println("File uploaded to: " + filepath);
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
            System.out.println("filepath: " + filepath);
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();
            externalContext.responseReset();

            try (InputStream inputStream = StorageService.download(filepath);
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
