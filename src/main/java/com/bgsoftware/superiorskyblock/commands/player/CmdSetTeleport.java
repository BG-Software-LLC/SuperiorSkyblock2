package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;

public final class CmdSetTeleport implements IPermissibleCommand {

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
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.SET_HOME;
    }

    @Override
    public Locale getPermissionLackMessage() {
        return Locale.NO_SET_HOME_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        Location newLocation = superiorPlayer.getLocation();

        if (!island.isInsideRange(newLocation)) {
            Locale.TELEPORT_LOCATION_OUTSIDE_ISLAND.send(superiorPlayer);
            return;
        }

        island.setTeleportLocation(newLocation);
        Locale.CHANGED_TELEPORT_LOCATION.send(superiorPlayer);
    }

}
