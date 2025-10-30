package com.bgsoftware.superiorskyblock.module.logging;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ModuleLoggerFileHandler {

    public static Logger addToLogger(File logFile, Logger logger) {
        try {
            if (logFile.exists()) {
                logFile.delete();
            }

            logFile.getParentFile().mkdirs();
            logFile.createNewFile();

            AsyncFileHandler asyncFileHandler = new AsyncFileHandler(logFile.getAbsolutePath());
            asyncFileHandler.setLevel(Level.ALL);

            logger.addHandler(asyncFileHandler);
        } catch (IOException error) {
            error.printStackTrace();
        }

        return logger;
    }

    private static class AsyncFileHandler extends FileHandler {

        private final BlockingQueue<LogRecord> queue = new LinkedBlockingQueue<>(10000);
        private final FileHandler fileHandler;
        private final Thread worker;

        private volatile boolean running = true;

        private AsyncFileHandler(String pattern) throws IOException {
            fileHandler = new FileHandler(pattern, true);
            fileHandler.setFormatter(new DebugFormatter());

            this.worker = new Thread(() -> {
                try {
                    while (running || !queue.isEmpty()) {
                        LogRecord record = queue.poll(100, TimeUnit.MILLISECONDS);
                        if (record != null) {
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
