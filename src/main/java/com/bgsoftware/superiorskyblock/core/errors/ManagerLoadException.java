package com.bgsoftware.superiorskyblock.core.errors;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

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

    public static boolean handle(ManagerLoadException ex) {
        PluginDebugger.debug(ex);
        ex.printStackTrace();

        if (ex.getErrorLevel() == ErrorLevel.SERVER_SHUTDOWN) {
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

        ConsoleCommandSender sender = Bukkit.getConsoleSender();
        sender.sendMessage("[SuperiorSkyblock2] " + ChatColor.RED + "################################################");
        sender.sendMessage("[SuperiorSkyblock2] " + ChatColor.RED + "##                                            ##");
        sender.sendMessage("[SuperiorSkyblock2] " + ChatColor.RED + "## An error occured while loading the plugin! ##");
        sender.sendMessage("[SuperiorSkyblock2] " + ChatColor.RED + "##                                            ##");
        sender.sendMessage("[SuperiorSkyblock2] " + ChatColor.RED + "################################################");
        sender.sendMessage("[SuperiorSkyblock2] ");
        sender.sendMessage("[SuperiorSkyblock2] " + ChatColor.RED + "Error:");
        messageLines.forEach(line -> sender.sendMessage("[SuperiorSkyblock2] " + ChatColor.RED + line));
        sender.sendMessage("[SuperiorSkyblock2] ");
        sender.sendMessage("[SuperiorSkyblock2] " + ChatColor.RED + "StackTrace:");

        int linesCounter = 0;

        for (String stackTraceLine : stackTrace.toString().split("\n")) {
            if (linesCounter > messageLines.size()) {
                if (!messageLines.contains(stackTraceLine)) {
                    sender.sendMessage("[SuperiorSkyblock2] " + ChatColor.RED + stackTraceLine);
                }
            } else {
                linesCounter++;
            }
        }

        sender.sendMessage("[SuperiorSkyblock2] ");
        sender.sendMessage("[SuperiorSkyblock2] " + ChatColor.RED + "################################################");
    }

    public enum ErrorLevel {

        SERVER_SHUTDOWN,
        CONTINUE

    }

}
