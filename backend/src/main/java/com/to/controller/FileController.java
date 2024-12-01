package com.to.controller;

import com.to.model.FileDocument;
import com.to.service.FileService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @DeleteMapping("/deleteAll")
    public String deleteAllFiles() {
        try {
            fileService.deleteAllFiles();
            return "All files deleted successfully!";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/all")
    public List<FileDocument> getAllFiles() {
        return fileService.getAllFiles();
    }

    @GetMapping("/duplicates")
    public List<List<FileDocument>> getDuplicates() {
        return fileService.findDuplicates();
    }

    @GetMapping("/versions")
    public List<List<FileDocument>> getFileVersions(@RequestParam(defaultValue = "3") int threshold) {
        return fileService.findFileVersions(threshold);
    }




}
