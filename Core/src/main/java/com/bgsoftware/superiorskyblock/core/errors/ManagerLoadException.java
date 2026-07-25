package com.bgsoftware.superiorskyblock.core.errors;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import org.bukkit.Bukkit;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("all")
public class ManagerLoadException extends Exception {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final ErrorLevel errorLevel;

    public ManagerLoadException(String message, ErrorLevel errorLevel) {
        super(message == null ? "" : message);
        this.errorLevel = errorLevel;
    }

    public ManagerLoadException(Throwable cause, ErrorLevel errorLevel) {
        super(cause);
        this.errorLevel = errorLevel;
    }

    public ManagerLoadException(Throwable cause, String message, ErrorLevel errorLevel) {
        super(message, cause);
        this.errorLevel = errorLevel;
    }

    public static boolean handle(ManagerLoadException error) {
        error.printStackTrace();

        if (error.getErrorLevel() == ErrorLevel.SERVER_SHUTDOWN) {
            Bukkit.shutdown();
            return false;
        }

        return true;
    }

    public ErrorLevel getErrorLevel() {
        return errorLevel;
    }

    @Override
    public void printStackTrace() {
        if (getErrorLevel() == ErrorLevel.CONTINUE) {
            super.printStackTrace();
            return;
        }

        StringWriter stackTrace = new StringWriter();
        super.printStackTrace(new PrintWriter(stackTrace));

        List<String> messageLines = Arrays.asList(getMessage().split("\n"));

        Log.error("################################################");
        Log.error("##                                            ##");
        Log.error("## An error occured while loading the plugin! ##");
        Log.error("##                                            ##");
        Log.error("################################################");
        if (!messageLines.isEmpty()) {
            Log.error(" ");
            messageLines.forEach(line -> Log.error(line));
        }
        Log.error(" ");
        Log.error("Stack Trace:");

        int linesCounter = 0;

        for (String stackTraceLine : stackTrace.toString().split("\n")) {
            if (linesCounter > messageLines.size()) {
                if (!messageLines.contains(stackTraceLine)) {
                    Log.error(stackTraceLine);
                }
            } else {
                linesCounter++;
            }
        }

        Log.error(" ");
        Log.error("################################################");
    }

    public enum ErrorLevel {

        SERVER_SHUTDOWN,
        CONTINUE

    }

}
