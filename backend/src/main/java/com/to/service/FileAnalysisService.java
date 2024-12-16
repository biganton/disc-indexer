package com.to.service;

import com.to.logic.CSVHandler;
import com.to.logic.EditDistanceCalculator;
import com.to.model.FileDocument;
import com.to.repository.FileRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileAnalysisService {

    private final FileRepository fileRepository;

    public FileAnalysisService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public List<FileDocument> findLargestFiles(int limit) {
        return fileRepository.findAll()
                .stream()
                .sorted(Comparator.comparingLong(FileDocument::getSize).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<List<FileDocument>> findDuplicates() {
        Map<String, List<FileDocument>> groupedByHash = fileRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(FileDocument::getHash));

        return groupedByHash.values().stream()
                .filter(group -> group.size() > 1)
                .collect(Collectors.toList());
    }

    public List<List<FileDocument>> findFileVersions(int threshold) {
        List<FileDocument> allFiles = fileRepository.findAll();
        List<List<FileDocument>> versions = new ArrayList<>();
        Set<FileDocument> processedFiles = new HashSet<>();
        List<String> keywords = CSVHandler.getKeywords();

        for (FileDocument file1 : allFiles) {
            if (processedFiles.contains(file1)) {
                continue;
            }

            List<FileDocument> similarFiles = new ArrayList<>();
            similarFiles.add(file1);

            for (FileDocument file2 : allFiles) {
                if (!file1.equals(file2) && !processedFiles.contains(file2)) {
                    String name = file2.getFileName();
                    for (String word : keywords) {
                        name = name.replace(word, "");
                    }
                    int distance = EditDistanceCalculator.calculate(file1.getFileName(), name);
                    if (distance <= threshold) {
                        similarFiles.add(file2);
                    }
                }
            }

            if (similarFiles.size() > 1) {
                versions.add(similarFiles);
                processedFiles.addAll(similarFiles);
            }
        }

        return versions;
    }
}
