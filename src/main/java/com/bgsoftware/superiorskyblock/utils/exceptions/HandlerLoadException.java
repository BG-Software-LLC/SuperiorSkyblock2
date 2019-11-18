package com.bgsoftware.superiorskyblock.utils.exceptions;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

import java.io.PrintWriter;
import java.io.StringWriter;

@SuppressWarnings("all")
public class HandlerLoadException extends Exception {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final ErrorLevel errorLevel;

    public HandlerLoadException(String message, ErrorLevel errorLevel){
        super(message);
        this.errorLevel = errorLevel;
    }

    public HandlerLoadException(Throwable cause, ErrorLevel errorLevel){
        super(cause);
        this.errorLevel = errorLevel;
    }

    public HandlerLoadException(Throwable cause, String message, ErrorLevel errorLevel){
        super(message, cause);
        this.errorLevel = errorLevel;
    }

    public ErrorLevel getErrorLevel() {
        return errorLevel;
    }

    @Override
    public void printStackTrace() {
        if(getErrorLevel() == ErrorLevel.CONTINUE){
            super.printStackTrace();
            return;
        }

        StringWriter stackTrace = new StringWriter();
        super.printStackTrace(new PrintWriter(stackTrace));

        ConsoleCommandSender sender = Bukkit.getConsoleSender();
        sender.sendMessage("[SuperiorSkyblock2] " + ChatColor.RED + "################################################");
        sender.sendMessage("[SuperiorSkyblock2] " + ChatColor.RED + "##                                            ##");
        sender.sendMessage("[SuperiorSkyblock2] " + ChatColor.RED + "## An error occured while loading the plugin! ##");
        sender.sendMessage("[SuperiorSkyblock2] " + ChatColor.RED + "##                                            ##");
        sender.sendMessage("[SuperiorSkyblock2] " + ChatColor.RED + "################################################");
        sender.sendMessage("[SuperiorSkyblock2] ");
        sender.sendMessage("[SuperiorSkyblock2] " + ChatColor.RED + "Error:");
        sender.sendMessage("[SuperiorSkyblock2] " + ChatColor.RED + getMessage());
        sender.sendMessage("[SuperiorSkyblock2] ");
        sender.sendMessage("[SuperiorSkyblock2] " + ChatColor.RED + "StackTrace:");

        boolean firstLine = true;

        for(String stackTraceLine : stackTrace.toString().split("\n")) {
            if(!firstLine) {
                sender.sendMessage("[SuperiorSkyblock2] " + ChatColor.RED + stackTraceLine);
            }else{
                firstLine = false;
            }
        }
    }

    public static boolean handle(HandlerLoadException ex){
        ex.printStackTrace();

        switch (ex.getErrorLevel()){
            case PLUGIN_SHUTDOWN:
                Executor.sync(() -> Bukkit.getPluginManager().disablePlugin(plugin));
                return false;
            case SERVER_SHUTDOWN:
                Bukkit.shutdown();
                return false;
        }

        return true;
    }

    public enum ErrorLevel{

        SERVER_SHUTDOWN,
        PLUGIN_SHUTDOWN,
        CONTINUE

    }

}
