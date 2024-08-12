package com.example.fileuploadtester.test;

import com.dsi.storage.core.StorageService;
import com.dsi.storage.dto.FileData;
import com.dsi.storage.exception.StorageException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.tika.mime.MimeTypes;
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
        FileData fileData = storageService.download(filePath);
        InputStream inputStream = fileData.inputStream();
        if (inputStream == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        byte[] bytes = inputStream.readAllBytes();
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(bytes));
        MimeTypes mimeTypes = MimeTypes.getDefaultMimeTypes();
        MediaType mediaType = MediaType.parseMediaType(fileData.contentType());
        String fileExtension = mimeTypes.forName(mediaType.toString()).getExtension();

        fileExtension = fileExtension != null ? fileExtension : "";


        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1) + fileExtension;

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(bytes.length)
                .body(resource);
    }

    @GetMapping("/image-manual-response")
    public void getImageAsByteArray(HttpServletResponse response) throws IOException, StorageException {
        FileData fileData = storageService.download("bucketname/nested/folder/image.png");
        InputStream in = fileData.inputStream();
        response.setContentType(fileData.contentType());
        IOUtils.copy(in, response.getOutputStream());
    }


    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) throws IOException, StorageException {
        String bucket = "testsssc";
        return "redirect:/?filePath=" + storageService.upload(bucket, file.getInputStream(), file.getContentType());
    }
}