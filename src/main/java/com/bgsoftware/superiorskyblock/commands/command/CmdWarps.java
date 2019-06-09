package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.gui.menus.types.warps.WarpsMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CmdWarps implements ICommand {
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("warps");
    }

    @Override
    public String getPermission() {
        return "superior.island.warps";
    }

    @Override
    public String getUsage() {
        return "island warps";
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
        new WarpsMenu((Player) sender);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
