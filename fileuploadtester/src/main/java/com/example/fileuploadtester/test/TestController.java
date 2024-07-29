package com.example.fileuploadtester.test;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class TestController {
    private final FileStorageService storageService;

    public TestController(FileStorageService storageService) {
        this.storageService = storageService;
    }


    @GetMapping("/")
    public String index(Model model) {
        return "test";
    }


    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        System.out.println("Enters handleFileUpload");
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();

        try {
            System.out.println("Start File Upload");
            storageService.upload(fileName, file.getInputStream(), contentType);
            System.out.println("End File Upload");

            redirectAttributes.addFlashAttribute("message", "File uploaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Error uploading file: " + e.getMessage());
        }

        return "redirect:/";
    }
}