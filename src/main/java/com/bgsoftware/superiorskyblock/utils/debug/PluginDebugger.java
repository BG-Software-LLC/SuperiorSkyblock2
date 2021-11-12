package com.bgsoftware.superiorskyblock.utils.debug;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PluginDebugger {

    private static final long TIMER_WRITE_DELAY = TimeUnit.SECONDS.toMillis(1);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private final File debugFile;
    private final AtomicBoolean debuggedMessage = new AtomicBoolean(false);

    private StringBuilder debugLines = new StringBuilder();
    private Timer writeDebugTimer;

    public PluginDebugger(File debugFile) {
        if (debugFile.exists())
            debugFile.delete();

        try {
            debugFile.getParentFile().mkdirs();
            debugFile.createNewFile();
        } catch (IOException error) {
            SuperiorSkyblockPlugin.log("&cError occurred while creating debug file. Disabling debugs...");
            debugFile = null;
        }

        this.debugFile = debugFile;
        this.writeDebugTimer = createWriteTimer();
    }

    public void debug(String message) {
        String formattedMessage = "[" + dateFormat.format(new Date()) + "]: " + message;
        if (this.debugFile != null) {
            debuggedMessage.set(true);
            synchronized (PluginDebugger.this) {
                this.debugLines.append(formattedMessage).append(System.lineSeparator());
            }
        }
    }

    public void cancel() {
        if (this.writeDebugTimer != null) {
            this.writeDebugTimer.cancel();
            this.writeDebugTimer = null;
        }
    }

    private Timer createWriteTimer() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new WriteDebugMessages(), TIMER_WRITE_DELAY, TIMER_WRITE_DELAY);
        return timer;
    }

    private void writeDebugMessages() {
        if(!debuggedMessage.get())
            return;

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(PluginDebugger.this.debugFile, "rw")) {
            randomAccessFile.seek(randomAccessFile.length());

            byte[] debugLines;
            synchronized (PluginDebugger.this) {
                debugLines = PluginDebugger.this.debugLines.toString().getBytes(StandardCharsets.UTF_8);
                PluginDebugger.this.debugLines = new StringBuilder();
            }

            randomAccessFile.write(debugLines);
            debuggedMessage.set(false);
        } catch (IOException ignored) {
        }
    }

    private class WriteDebugMessages extends TimerTask {

        @Override
        public void run() {
            writeDebugMessages();
        }
    }

}
