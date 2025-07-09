package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Locale;

public interface ISuperiorCommand extends SuperiorCommand {

    @Override
    default String getUsage(Locale locale) {
        return "";
    }

    @Override
    default String getUsage(SuperiorSkyblock plugin, CommandSender sender, Locale locale) {
        return getUsage((SuperiorSkyblockPlugin) plugin, sender, locale);
    }

    default String getUsage(SuperiorSkyblockPlugin plugin, CommandSender sender, Locale locale) {
        return getUsage(locale);
    }

    @Override
    default int getMinArgs() {
        return -1;
    }

    @Override
    default int getMinArgs(SuperiorSkyblock plugin, CommandSender sender) {
        return getMinArgs((SuperiorSkyblockPlugin) plugin, sender);
    }

    default int getMinArgs(SuperiorSkyblockPlugin plugin, CommandSender sender) {
        return getMinArgs();
    }

    @Override
    default int getMaxArgs() {
        return -1;
    }

    @Override
    default int getMaxArgs(SuperiorSkyblock plugin, CommandSender sender) {
        return getMaxArgs((SuperiorSkyblockPlugin) plugin, sender);
    }

    default int getMaxArgs(SuperiorSkyblockPlugin plugin, CommandSender sender) {
        return getMaxArgs();
    }

    @Override
    default boolean displayCommand() {
        return true;
    }

    default void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        execute((SuperiorSkyblockPlugin) plugin, sender, args);
    }

    default List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return tabComplete((SuperiorSkyblockPlugin) plugin, sender, args);
    }

    void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args);

    List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args);

}
