package com.bgsoftware.superiorskyblock.core.io;

import com.bgsoftware.superiorskyblock.core.logging.Log;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

public class Files {

    private static final Set<String> BLACKLISTED_FILE_NAMES = initializeBlacklistedFileNames();

    private static final Object mutex = new Object();

    private Files() {

    }

    public static void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] childFiles = directory.listFiles();
            if (childFiles != null) {
                for (File file : childFiles)
                    deleteDirectory(file);
            }
        }

        //noinspection ResultOfMethodCallIgnored
        directory.delete();
    }

    public static void replaceString(File file, String str, String replace) {
        synchronized (mutex) {
            StringBuilder stringBuilder = new StringBuilder();

            try {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null)
                        stringBuilder.append("\n").append(line);
                }

                if (stringBuilder.length() > 0) {
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(stringBuilder.substring(1).replace(str, replace));
                    }
                }
            } catch (Exception error) {
                Log.entering("ENTER", file.getName(), str, replace);
                Log.error(error, "An unexpected error occurred while replacing strings in file:");
            }
        }
    }

    public static List<File> listFolderFiles(File folder, boolean recursive) {
        return listFolderFiles(folder, recursive, null);
    }

    public static List<File> listFolderFiles(File folder, boolean recursive, @Nullable Predicate<File> predicate) {
        if (!folder.isDirectory())
            return Collections.emptyList();

        List<File> folderFiles = new LinkedList<>();

        File[] folderFilesArr = folder.listFiles();
        if (folderFilesArr != null && folderFilesArr.length > 0) {
            for (File file : folderFilesArr) {
                if (file.isDirectory()) {
                    if (recursive)
                        folderFiles.addAll(listFolderFiles(folder, true, predicate));
                } else if (!BLACKLISTED_FILE_NAMES.contains(file.getName().toLowerCase(Locale.ENGLISH)) &&
                        (predicate == null || predicate.test(file))) {
                    folderFiles.add(file);
                }
            }
        }

        return folderFiles.isEmpty() ? Collections.emptyList() : folderFiles;
    }

    public static String getFileName(File file) {
        String[] fileNameSections = file.getName().split("\\.");
        return fileNameSections.length == 1 ? fileNameSections[0] :
                file.getName().replace("." + fileNameSections[fileNameSections.length - 1], "");
    }

    private static Set<String> initializeBlacklistedFileNames() {
        return Collections.singleton(".ds_store");
    }

}
