package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;

import java.util.Arrays;
import java.util.List;

public final class CmdOpen implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("open", "unlock");
    }

    @Override
    public String getPermission() {
        return "superior.island.open";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "open";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_OPEN.getMessage(locale);
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
        return IslandPrivileges.OPEN_ISLAND;
    }

    @Override
    public Locale getPermissionLackMessage() {
        return Locale.NO_OPEN_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        island.setLocked(false);
        Locale.ISLAND_OPENED.send(superiorPlayer);
    }

}
