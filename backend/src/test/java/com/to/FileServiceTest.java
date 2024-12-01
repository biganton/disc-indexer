package com.to;

import com.to.model.FileDocument;
import com.to.repository.FileRepository;
import com.to.service.FileService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private FileService fileService;

    @Test
    void testFindDuplicates() {
        // given
        FileDocument file1 = new FileDocument();
        file1.setHash("hash123");
        file1.setFileName("file1.txt");

        FileDocument file2 = new FileDocument();
        file2.setHash("hash123");
        file2.setFileName("file2.txt");

        FileDocument file3 = new FileDocument();
        file3.setHash("hash456");
        file3.setFileName("file3.txt");

        List<FileDocument> allFiles = List.of(file1, file2, file3);
        Mockito.when(fileRepository.findAll()).thenReturn(allFiles);

        // when
        List<List<FileDocument>> duplicates = fileService.findDuplicates();

        // then
        Assertions.assertEquals(1, duplicates.size());
        Assertions.assertEquals(2, duplicates.getFirst().size());
        Assertions.assertTrue(duplicates.getFirst().contains(file1));
        Assertions.assertTrue(duplicates.getFirst().contains(file2));
    }

    @Test
    void testFindFileVersions() {
        // given
        FileDocument file1 = new FileDocument();
        file1.setFileName("file1.txt");

        FileDocument file2 = new FileDocument();
        file2.setFileName("file1_v1.txt");

        FileDocument file3 = new FileDocument();
        file3.setFileName("file2.txt");

        List<FileDocument> allFiles = List.of(file1, file2, file3);
        Mockito.when(fileRepository.findAll()).thenReturn(allFiles);

        // when
        List<List<FileDocument>> versions = fileService.findFileVersions(2);

        // then
        Assertions.assertEquals(1, versions.size());
        Assertions.assertEquals(2, versions.getFirst().size());
        Assertions.assertTrue(versions.getFirst().contains(file1));
        Assertions.assertTrue(versions.getFirst().contains(file3));
    }

}
