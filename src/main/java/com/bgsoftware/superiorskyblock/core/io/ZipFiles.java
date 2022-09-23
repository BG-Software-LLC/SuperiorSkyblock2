package com.bgsoftware.superiorskyblock.core.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFiles {

    private ZipFiles() {

    }

    public static void zipFolder(File input, File output) throws IOException {
        if (!input.isDirectory())
            throw new IOException("Input file must be a directory.");

        try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(output.toPath()))) {
            zipFolderInternal(input, "", outputStream);
        }
    }

    public static void zipFile(File input, File output) throws IOException {
        if (input.isDirectory())
            throw new IOException("Input file must be a file.");

        try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(output.toPath()))) {
            zipFileInternal(input, input.getName(), outputStream);
        }
    }

    private static void zipFolderInternal(File input, String parent, ZipOutputStream outputStream) throws IOException {
        // Add the folder as an entry
        String folderPath = parent + input.getName() + File.separator;
        ZipEntry zipEntry = new ZipEntry(folderPath);
        outputStream.putNextEntry(zipEntry);
        outputStream.closeEntry();

        for (File innerFile : input.listFiles()) {
            if (innerFile.isDirectory()) {
                zipFolderInternal(innerFile, folderPath, outputStream);
            } else {
                zipFileInternal(innerFile, folderPath + innerFile.getName(), outputStream);
            }
        }
    }

    private static void zipFileInternal(File input, String zipEntryName, ZipOutputStream outputStream) throws IOException {
        ZipEntry zipEntry = new ZipEntry(zipEntryName);
        try {
            outputStream.putNextEntry(zipEntry);
            Files.copy(input.toPath(), outputStream);
        } finally {
            outputStream.closeEntry();
        }
    }

}
