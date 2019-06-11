package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.ConfirmDisbandMenu;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdDisband implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("disband");
    }

    @Override
    public String getPermission() {
        return "superior.island.disband";
    }

    @Override
    public String getUsage() {
        return "island disband";
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
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        if(!superiorPlayer.hasPermission(IslandPermission.DISBAND_ISLAND)){
            Locale.NO_DISBAND_PERMISSION.send(superiorPlayer, island.getRequiredRole(IslandPermission.DISBAND_ISLAND));
            return;
        }

        if (!superiorPlayer.hasDisbands()) {
            Locale.NO_MORE_DISBANDS.send(superiorPlayer);
            return;
        }

        ConfirmDisbandMenu.openInventory(superiorPlayer, null);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
