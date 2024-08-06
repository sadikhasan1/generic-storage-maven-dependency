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
    private final StorageService storageService;

    public TestController() {
        this.storageService = StorageService.init();
    }

    @GetMapping("/")
    public String index(
            @RequestParam(defaultValue = "") String filePath,
            Model model) {
        model.addAttribute("filePath", filePath);
        return "test";
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String filePath) throws Exception{
        return storageService.downloadAsResponseEntityForResource(filePath);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        String bucket = "just/atest/for/nested";
        return "redirect:/?filePath=" + storageService.upload(bucket, file);
    }
}