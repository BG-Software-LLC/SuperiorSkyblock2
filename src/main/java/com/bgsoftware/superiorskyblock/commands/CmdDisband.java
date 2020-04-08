package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.menu.MenuConfirmDisband;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdDisband implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("disband", "reset");
    }

    @Override
    public String getPermission() {
        return "superior.island.disband";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "disband";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_DISBAND.getMessage(locale);
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

        if(!superiorPlayer.hasPermission(IslandPrivileges.DISBAND_ISLAND)){
            Locale.NO_DISBAND_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.DISBAND_ISLAND));
            return;
        }

        if (!superiorPlayer.hasDisbands() && plugin.getSettings().disbandCount > 0) {
            Locale.NO_MORE_DISBANDS.send(superiorPlayer);
            return;
        }

        if(plugin.getSettings().disbandConfirm) {
            MenuConfirmDisband.openInventory(superiorPlayer, null);
        }

        else if(EventsCaller.callIslandDisbandEvent(superiorPlayer, island)){
            ((SIsland) island).sendMessage(Locale.DISBAND_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName());

            Locale.DISBANDED_ISLAND.send(superiorPlayer);

            superiorPlayer.setDisbands(superiorPlayer.getDisbands() - 1);
            island.disbandIsland();
        }

    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
