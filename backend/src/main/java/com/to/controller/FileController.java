package com.to.controller;

import com.to.model.FileDocument;
import com.to.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
@Tag(name = "File Operations", description = "Endpoints for managing files")
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
    @Operation(summary = "Scan folder", description = "Scans a folder and saves file data to the database.")
    @ApiResponse(responseCode = "200", description = "Folder scanned successfully")
    public String scanFolder(@RequestParam String folderPath) {
        try {
            fileService.processFolder(folderPath);
            return "Directory scanned successfully!";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @DeleteMapping("/deleteAll")
    @Operation(summary = "Delete all files", description = "Deletes all files from the database.")
    public String deleteAllFiles() {
        try {
            fileService.deleteAllFiles();
            return "All files deleted successfully!";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/all")
    @Operation(
            summary = "Get all files",
            description = "Returns a list of all files stored in the database."
    )
    @ApiResponse(responseCode = "200", description = "List of all files retrieved successfully.")
    public List<FileDocument> getAllFiles() {
        return fileService.getAllFiles();
    }

    @GetMapping("/duplicates")
    @Operation(
            summary = "Find duplicate files",
            description = "Returns groups of files with identical content based on hash comparison."
    )
    @ApiResponse(responseCode = "200", description = "List of duplicate file groups retrieved successfully.")
    @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")
    public List<List<FileDocument>> getDuplicates() {
        return fileService.findDuplicates();
    }

    @GetMapping("/versions")
    @Operation(
            summary = "Find file versions",
            description = "Returns groups of files that are considered versions of each other based on Edit Distance of their filenames."
    )
    @ApiResponse(responseCode = "200", description = "List of file version groups retrieved successfully.")
    public List<List<FileDocument>> getFileVersions(@RequestParam(defaultValue = "3") int threshold) {
        return fileService.findFileVersions(threshold);
    }

    @GetMapping("/largest")
    @Operation(
            summary = "Find largest files",
            description = "Returns largest files in a directory."
    )
    @ApiResponse(responseCode = "200", description = "List of file largest files retrieved successfully.")
    public List<FileDocument> getLargestFiles(@RequestParam(defaultValue = "10") int limit) {
        return fileService.findLargestFiles(limit);
    }

    @PostMapping("/open")
    @Operation(
            summary = "Open file",
            description = "Opens file"
    )
    @ApiResponse(responseCode = "200", description = "File open successfully.")
    @ApiResponse(responseCode = "500", description = "Could not open a file")
    public void openFile(@RequestBody Map<String, String> request) throws IOException {
        String filePath = request.get("filePath");
        fileService.openFile(filePath);
    }

    @DeleteMapping("/delete")
    @Operation(
            summary = "Delete file",
            description = "Deletes file"
    )
    @ApiResponse(responseCode = "200", description = "File deleted successfully.")
    @ApiResponse(responseCode = "500", description = "Could not delete a file")
    public void deleteFile(@RequestBody Map<String, String> request) throws IOException {
        String fileId = request.get("id");
        fileService.deleteFile(fileId);
    }
}
