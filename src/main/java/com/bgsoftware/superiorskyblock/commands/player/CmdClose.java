package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;

import java.util.Arrays;
import java.util.List;

public class CmdClose implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("close", "lock");
    }

    @Override
    public String getPermission() {
        return "superior.island.close";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "close";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_CLOSE.getMessage(locale);
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
        return IslandPrivileges.CLOSE_ISLAND;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_CLOSE_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        if (plugin.getEventsBus().callIslandCloseEvent(island, superiorPlayer)) {
            island.setLocked(true);
            Message.ISLAND_CLOSED.send(superiorPlayer);
        }
    }

}
