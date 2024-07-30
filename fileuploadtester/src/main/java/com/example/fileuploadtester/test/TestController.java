package com.example.fileuploadtester.test;

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
    private final FileStorageService storageService;

    public TestController(FileStorageService storageService) {
        this.storageService = storageService;
    }


    @GetMapping("/")
    public String index(
            @RequestParam(defaultValue = "") String bucketName,
            @RequestParam(defaultValue = "") String objectName,
            Model model) {
        model.addAttribute("bucketName", bucketName);
        model.addAttribute("objectName", objectName);
        return "test";
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String bucketName,
            @RequestParam String objectName) throws Exception{
        try {
            // Download file from storage service
            InputStream inputStream = storageService.download(bucketName, objectName);

            // Convert InputStream to byte[]
            byte[] fileContent = convertInputStreamToByteArray(inputStream);

            // Convert byte array to InputStreamResource
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(fileContent));

            // Build response with the file content
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + objectName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(fileContent.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (IOException e) {
            // Handle error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    private byte[] convertInputStreamToByteArray(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        }
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        System.out.println("Enters handleFileUpload");
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        String bucket = "justjjjjjjjjjjjj";

        try {
            System.out.println("Start File Upload");
            storageService.upload(bucket, fileName, file.getInputStream(), contentType);
            System.out.println("End File Upload");

            redirectAttributes.addFlashAttribute("message", "File uploaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Error uploading file: " + e.getMessage());
        }

        return "redirect:/?bucketName=" + bucket + "&objectName=" + fileName;
    }
}