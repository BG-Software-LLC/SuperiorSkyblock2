package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface ISuperiorCommand extends SuperiorCommand {

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
