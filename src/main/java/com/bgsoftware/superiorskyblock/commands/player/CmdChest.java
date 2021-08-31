package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;

import java.util.Arrays;
import java.util.List;

public final class CmdChest implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("chest", "vault");
    }

    @Override
    public String getPermission() {
        return "superior.island.chest";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "chest";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_CHEST.getMessage(locale);
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
        return IslandPrivileges.ISLAND_CHEST;
    }

    @Override
    public Locale getPermissionLackMessage() {
        return Locale.NO_ISLAND_CHEST_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        plugin.getMenus().openIslandChest(superiorPlayer, null, island);
    }

}
