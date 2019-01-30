package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface ICommand {

    List<String> getAliases();

    String getPermission();

    String getUsage();

    int getMinArgs();

    int getMaxArgs();

    boolean canBeExecutedByConsole();

    void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args);

    List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args);

}
