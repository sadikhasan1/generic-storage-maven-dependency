package com.dsi.fileupload.jsf;

import com.dsi.storage.dto.FileData;
import com.dsi.storage.exception.StorageException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.primefaces.model.file.UploadedFile;
import com.dsi.storage.core.StorageService;

import java.io.*;
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
                filepath = storageService.upload("random/for/test", inputStream, file.getContentType());
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



                MimeTypes mimeTypes = MimeTypes.getDefaultMimeTypes();
                MediaType mediaType = MediaType.parse(fileData.contentType());
                String fileExtension = mimeTypes.forName(mediaType.toString()).getExtension();

                fileExtension = fileExtension != null ? fileExtension : "";

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
            } catch (MimeTypeException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
