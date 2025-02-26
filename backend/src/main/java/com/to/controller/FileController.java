package com.to.controller;

import com.to.model.FileDocument;
import com.to.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "Scan directory", description = "Scans a directory and saves file data to the database.")
    @ApiResponse(responseCode = "200", description = "Directory scanned successfully")
    public String scanDirectory(@RequestParam String directoryPath) {
        try {
            fileService.processDirectory(directoryPath);
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

    @PostMapping("/duplicates/move-to-grouped-directories")
    @Operation(
            summary = "Move duplicate files to grouped directories",
            description = "Moves each group of duplicate files to its own directory under the target path."
    )
    @ApiResponse(responseCode = "200", description = "Duplicates moved to grouped directories successfully.")
    @ApiResponse(responseCode = "400", description = "Bad request. Missing or invalid input parameters.")
    @ApiResponse(responseCode = "500", description = "Internal server error.")
    public ResponseEntity<String> moveDuplicatesToGroupedDirectories(@RequestBody Map<String, Object> request) {
        String targetDirectoryPath = (String) request.get("targetDirectoryPath");
        Boolean archive = (Boolean) request.get("archive");

        if (targetDirectoryPath == null || targetDirectoryPath.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing required field: targetDirectoryPath");
        }
        try {
            fileService.moveDuplicatesToGroupedDirectories(targetDirectoryPath);
            if (archive != null && archive) {
                fileService.archiveDirectory(targetDirectoryPath);
            }
            return ResponseEntity.ok(archive != null && archive
                    ? "Duplicates moved to grouped directories and directory archived successfully!"
                    : "Duplicates moved to grouped directories successfully!");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/versions/move-to-grouped-directories")
    @Operation(
            summary = "Move file versions to grouped directories",
            description = "Moves each group of file versions to its own directory under the target path."
    )
    @ApiResponse(responseCode = "200", description = "Versions moved to grouped directories successfully.")
    @ApiResponse(responseCode = "400", description = "Bad request. Missing or invalid input parameters.")
    @ApiResponse(responseCode = "500", description = "Internal server error.")
    public ResponseEntity<String> moveVersionsToGroupedDirectories(@RequestBody Map<String, Object> request) {
        String targetDirectoryPath = (String) request.get("targetDirectoryPath");
        Integer threshold = (Integer) request.getOrDefault("threshold", 3);
        Boolean archive = (Boolean) request.get("archive");

        if (targetDirectoryPath == null || targetDirectoryPath.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing required field: targetDirectoryPath");
        }

        try {
            fileService.moveVersionsToGroupedDirectories(targetDirectoryPath, threshold);
            if (archive != null && archive) {
                fileService.archiveDirectory(targetDirectoryPath);
            }
            return ResponseEntity.ok(archive != null && archive
                    ? "Versions moved to grouped directories and directory archived successfully!"
                    : "Versions moved to grouped directories successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }


    @PostMapping("/move-to-directory")
    @Operation(
            summary = "Move selected files to a grouped directory",
            description = "Moves the selected files to a specified directory. Optionally archives the directory."
    )
    @ApiResponse(responseCode = "200", description = "Files moved successfully.")
    @ApiResponse(responseCode = "400", description = "Bad request. Missing required fields: fileIds or targetDirectoryPath.")
    @ApiResponse(responseCode = "500", description = "Internal server error.")
    public ResponseEntity<String> moveSelectedFilesToDirectory(
            @RequestBody Map<String, Object> request) {

        List<String> fileIds = (List<String>) request.get("fileIds");
        String targetDirectoryPath = (String) request.get("targetDirectoryPath");
        Boolean archive = (Boolean) request.get("archive");

        if (fileIds == null || fileIds.isEmpty() || targetDirectoryPath == null || targetDirectoryPath.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing required fields: fileIds or targetDirectoryPath");
        }

        try {
            fileService.moveSelectedFilesToDirectory(fileIds, targetDirectoryPath);
            if (archive != null && archive) {
                fileService.archiveDirectory(targetDirectoryPath);
            }
            return ResponseEntity.ok(archive != null && archive
                    ? "Files moved and directory archived successfully!"
                    : "Files moved successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/archive")
    @Operation(
    summary = "Archives files",
    description = "Archives files from some directory into new path"
    )
    @ApiResponse(responseCode = "200", description = "The directory has been archived successfully.")
    @ApiResponse(responseCode = "500", description = "Internal server error.")
    public ResponseEntity<String> archiveDirectory(@RequestParam String directoryPath, @RequestParam String targetDirectoryPath) {
        try {
            fileService.archiveDirectory(directoryPath);
            return ResponseEntity.ok("The directory has been archived!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search for files by keyword",
            description = "Searches the database for files containing the specified keyword in their content. Only text-based files are considered."
    )
    @ApiResponse(responseCode = "200", description = "Search results returned successfully.")
    @ApiResponse(responseCode = "500", description = "Internal server error.")
    public ResponseEntity<List<FileDocument>> searchFilesByKeyword(@RequestParam String keyword) {
        try {
            List<FileDocument> results = fileService.searchFilesByKeyword(keyword);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
