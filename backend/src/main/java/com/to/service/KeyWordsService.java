package com.to.service;

import com.to.logic.CSVHandler;
import com.to.logic.EditDistanceCalculator;
import com.to.model.FileDocument;
import com.to.repository.FileRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeyWordsService {

    private final FileRepository fileRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public KeyWordsService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @PostConstruct
    public void createTextIndex() {
        TextIndexDefinition textIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
                .onField("content")
                .build();
        mongoTemplate.indexOps(FileDocument.class).ensureIndex(textIndex);
    }
    public List<FileDocument> searchFilesByKeyword(String keyword) {
        return fileRepository.searchByKeyword(keyword);
    }
}