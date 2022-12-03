package com.bgsoftware.superiorskyblock.core.io;

import com.bgsoftware.superiorskyblock.core.logging.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class Files {

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

}
