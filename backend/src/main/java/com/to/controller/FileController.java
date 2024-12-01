package com.to.controller;

import com.to.service.FileService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }
    @GetMapping
    public String greeting() {
        return "FileController greeting!";
    }

    @PostMapping("/scan")
    public String scanFolder(@RequestParam String folderPath) {
        try {
            fileService.processFolder(folderPath);
            return "Directory scanned successfully!";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

}
