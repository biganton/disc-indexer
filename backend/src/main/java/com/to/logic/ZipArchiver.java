package com.to.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class ZipArchiver {

    public void zipFolderAndDeleteOriginal(String sourceDirPath, String zipFilePath) throws IOException {
        File folderToZip = new File(sourceDirPath);
        zipFolder(sourceDirPath, zipFilePath);
        deleteFolder(folderToZip);
    }

    public void zipFolder(String sourceDirPath, String zipFilePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(zipFilePath);
        File folderToZip = new File(sourceDirPath);
        try (fos; ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            zipFiles(folderToZip, folderToZip.getName(), zipOut);
        }
    }

    private void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file);
                }
            }
        }
        if (!folder.delete()) {
            throw new RuntimeException("Failed to delete folder: " + folder.getAbsolutePath());
        }
    }

    private void zipFiles(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            assert children != null;
            for (File childFile : children) {
                zipFiles(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        } catch (IOException e) {
            throw new IOException("Failed to zip file: " + fileToZip.getName(), e);
        } finally {
            zipOut.closeEntry();
            assert fis != null;
            fis.close();
        }
    }
}
