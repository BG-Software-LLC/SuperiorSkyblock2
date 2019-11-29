package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.menu.IslandsTopMenu;
import com.bgsoftware.superiorskyblock.utils.islands.SortingTypes;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.command.CommandSender;

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
        return "top";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_TOP.getMessage();
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
        IslandsTopMenu.openInventory(SSuperiorPlayer.of(sender), null, SortingTypes.DEFAULT);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
