package com.example.fileuploadtester.test;

import com.dsi.storage.core.StorageService;
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
    @Value("${storage.bucket}")
    private String bucket;

    private final StorageService storageService;

    public TestController() {
        this.storageService = StorageService.init();
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
        return storageService.downloadAsResponseEntityForResource(bucketName, objectName);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

        try {
            System.out.println("Start File Upload");
            String bucketName = "my-bucket"; // Replace with your bucket/container name
            String objectName = file.getOriginalFilename();
            storageService.upload(bucketName, objectName, file);
            System.out.println("End File Upload");

            redirectAttributes.addFlashAttribute("message", "File uploaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Error uploading file: " + e.getMessage());
        }

        return "redirect:/?bucketName=" + bucket + "&objectName=" + fileName;
    }
}