package com.to.controller;

import com.to.model.FileDocument;
import com.to.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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




}
