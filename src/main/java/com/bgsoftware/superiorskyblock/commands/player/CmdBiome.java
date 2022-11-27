package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.Arrays;
import java.util.List;

public class CmdBiome implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("biome", "setbiome");
    }

    @Override
    public String getPermission() {
        return "superior.island.biome";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "biome";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_BIOME.getMessage(locale);
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
        return IslandPrivileges.SET_BIOME;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_SET_BIOME_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        plugin.getMenus().openBiomes(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()), superiorPlayer.getIsland());
    }

}
