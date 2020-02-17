package com.bgsoftware.superiorskyblock.utils.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public final class CommandUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static void dispatchSubCommand(CommandSender sender, String subCommand){
        String commandLabel = plugin.getSettings().islandCommand.split(",")[0];
        Bukkit.dispatchCommand(sender, commandLabel + " " + subCommand);
    }

}
