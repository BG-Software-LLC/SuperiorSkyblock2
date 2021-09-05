package com.bgsoftware.superiorskyblock.utils.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public final class CommandUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private CommandUtils(){

    }

    public static void dispatchSubCommand(CommandSender sender, String subCommand){
        dispatchSubCommand(sender, subCommand, "");
    }

    public static void dispatchSubCommand(CommandSender sender, String subCommand, String args){
        String commandLabel = plugin.getSettings().getIslandCommand().split(",")[0];
        Bukkit.dispatchCommand(sender, commandLabel + " " + subCommand + (args.isEmpty() ? "" : " " + args));
    }

    public static void dispatchAdminSubCommand(String subCommand, String args){
        String commandLabel = plugin.getSettings().getIslandCommand().split(",")[0];
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandLabel + " admin " + subCommand + (args.isEmpty() ? "" : " " + args));
    }

}
