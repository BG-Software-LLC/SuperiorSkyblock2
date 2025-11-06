package com.bgsoftware.superiorskyblock.module.logging;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ModuleLoggerFileHandler {

    private static final SimpleDateFormat ARCHIVE_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
    private static final Path BASE_LOGS_FOLDER = new File(SuperiorSkyblockPlugin.getPlugin().getDataFolder(), "logs").toPath();

    public static Logger addToLogger(File logsFolder, File archiveFolder, Logger logger) {
        try {
            logsFolder.mkdirs();

            File logsFile = new File(logsFolder, "latest.log");

            long startOfDayTime = LocalDate.now().atStartOfDay(ZoneOffset.systemDefault()).toEpochSecond() * 1000;

            if (logsFile.exists() && logsFile.lastModified() < startOfDayTime) {
                // Save old file
                File dateLogsFileZipped = new File(archiveFolder, ARCHIVE_DATE_FORMAT.format(new Date(logsFile.lastModified())) + ".zip");
                zipLogsFile(logsFile, BASE_LOGS_FOLDER.relativize(logsFile.toPath()).toString(), dateLogsFileZipped);
            }

            AsyncFileHandler asyncFileHandler = new AsyncFileHandler(archiveFolder);
            asyncFileHandler.setFile(logsFile);
            asyncFileHandler.setLevel(Level.ALL);

            logger.addHandler(asyncFileHandler);
        } catch (IOException error) {
            error.printStackTrace();
        }

        return logger;
    }

    private static void zipLogsFile(File file, String name, File zipFile) {
        zipFile.getParentFile().mkdirs();

        try {
            File tempFile = new File(zipFile.getAbsolutePath() + ".tmp");

            byte[] buffer = new byte[1024];

            try (ZipInputStream zis = zipFile.exists() ? new ZipInputStream(new FileInputStream(zipFile)) : null;
                 ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempFile))) {

                ZipEntry entry;

                if (zis != null) {
                    // Copy existing entries
                    while ((entry = zis.getNextEntry()) != null) {
                        ZipEntry newEntry = new ZipEntry(entry.getName());
                        zos.putNextEntry(newEntry);
                        newEntry.setLastModifiedTime(entry.getLastModifiedTime());
                        int len;
                        while ((len = zis.read(buffer)) != -1) {
                            zos.write(buffer, 0, len);
                        }
                        zos.closeEntry();
                    }
                }

                // Create new zip entry
                ZipEntry zipEntry = new ZipEntry(name);
                zos.putNextEntry(zipEntry);
                zipEntry.setLastModifiedTime(FileTime.fromMillis(file.lastModified()));
                zipEntry.setLastAccessTime(FileTime.fromMillis(file.lastModified()));
                try (FileInputStream input = new FileInputStream(file)) {
                    int length;
                    while ((length = input.read(buffer)) >= 0) {
                        zos.write(buffer, 0, length);
                    }
                }
                zos.closeEntry();
            } finally {
                zipFile.delete();
                tempFile.renameTo(zipFile);
            }
        } catch (Throwable error) {
            error.printStackTrace();
        }

        file.delete();
    }

    private static class AsyncFileHandler extends FileHandler {

        private final BlockingQueue<LogRecord> queue = new LinkedBlockingQueue<>(10000);
        private final Thread worker;
        private final File archiveFolder;

        private FileHandler fileHandler;
        private File logsFile;

        private volatile boolean running = true;

        private volatile long nextDayTime;

        private AsyncFileHandler(File archiveFolder) throws IOException {
            this.archiveFolder = archiveFolder;

            calculateTimeOfNextDay();

            this.worker = new Thread(() -> {
                try {
                    while (running || !queue.isEmpty()) {
                        LogRecord record = queue.poll(100, TimeUnit.MILLISECONDS);
                        if (record != null) {
                            if (System.currentTimeMillis() > nextDayTime) {
                                // Upgrade file
                                nextDayLogsFile();
                            }

                            fileHandler.publish(record);
                        }
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    fileHandler.flush();
                    fileHandler.close();
                }
            }, "AsyncLoggerThread");

            this.worker.setDaemon(true);
            this.worker.start();
        }

        private void calculateTimeOfNextDay() {
            this.nextDayTime = LocalDate.now().plusDays(1).atStartOfDay(ZoneOffset.systemDefault()).toEpochSecond() * 1000;
        }

        public void setFile(File file) throws IOException {
            file.createNewFile();
            this.fileHandler = new FileHandler(file.getAbsolutePath(), true);
            this.fileHandler.setFormatter(new DebugFormatter());
            this.logsFile = file;
        }

        private void nextDayLogsFile() {
            this.fileHandler.close();
            this.fileHandler = null;

            File dateLogsFileZipped = new File(archiveFolder, ARCHIVE_DATE_FORMAT.format(new Date(logsFile.lastModified())) + ".zip");
            zipLogsFile(logsFile, BASE_LOGS_FOLDER.relativize(logsFile.toPath()).toString(), dateLogsFileZipped);

            try {
                setFile(this.logsFile);
            } catch (Throwable error) {
                error.printStackTrace();
            }

            calculateTimeOfNextDay();
        }

        @Override
        public void publish(LogRecord record) {
            if (!running) return;
            this.queue.offer(record);
        }

        @Override
        public void flush() {
            this.fileHandler.flush();
        }

        @Override
        public void close() throws SecurityException {
            running = false;
            try {
                this.worker.join(1000);
            } catch (InterruptedException ignored) {
            }
        }

    }

    private static class DebugFormatter extends Formatter {

        private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

        @Override
        public String format(LogRecord record) {
            String time = DATE_FORMAT.format(new Date(record.getMillis()));
            String level = record.getLevel().getLocalizedName();
            return String.format("[%s %s]: %s%n", time, level, formatMessage(record));
        }

    }

}
