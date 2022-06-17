package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandSetHomeEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;

public class CmdSetTeleport implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
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
        return Message.COMMAND_DESCRIPTION_SET_TELEPORT.getMessage(locale);
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
    public Message getPermissionLackMessage() {
        return Message.NO_SET_HOME_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        Location newLocation = superiorPlayer.getLocation();

        if (!island.isInsideRange(newLocation)) {
            Message.TELEPORT_LOCATION_OUTSIDE_ISLAND.send(superiorPlayer);
            return;
        }

        EventResult<Location> eventResult = plugin.getEventsBus().callIslandSetHomeEvent(island, newLocation,
                IslandSetHomeEvent.Reason.SET_HOME_COMMAND, superiorPlayer);

        if (eventResult.isCancelled())
            return;

        island.setIslandHome(eventResult.getResult());
        Message.CHANGED_TELEPORT_LOCATION.send(superiorPlayer);
    }

}
