package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.gui.menus.types.statistics.TopIslandsMenu;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdTop implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("top");
    }

    @Override
    public String getPermission() {
        return "superior.island.top";
    }

    @Override
    public String getUsage() {
        return "island top";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
//        plugin.getGrid().openTopIslands(SSuperiorPlayer.of(sender));
        new TopIslandsMenu((Player) sender);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
