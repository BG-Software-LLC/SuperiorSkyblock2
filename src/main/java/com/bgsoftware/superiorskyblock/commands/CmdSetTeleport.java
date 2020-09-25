package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdSetTeleport implements ISuperiorCommand {

    @Override
    public List<String> getAliases(){
        return Arrays.asList("settp", "setteleport", "setgo", "sethome");
    }

    @Override
    public String getPermission() {
        return "superior.island.setteleport";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "setteleport";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_SET_TELEPORT.getMessage(locale);
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
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        Island island = superiorPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        Location newLocation = superiorPlayer.getLocation();

        if(!superiorPlayer.hasPermission(IslandPrivileges.SET_HOME)){
            Locale.NO_SET_HOME_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.SET_HOME));
            return;
        }

        if (!island.isInsideRange(newLocation)) {
            Locale.TELEPORT_LOCATION_OUTSIDE_ISLAND.send(superiorPlayer);
            return;
        }

        island.setTeleportLocation(newLocation);
        Locale.CHANGED_TELEPORT_LOCATION.send(superiorPlayer);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
