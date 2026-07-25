package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;

import java.util.Collections;
import java.util.List;

public class CmdRatings implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("ratings");
    }

    @Override
    public String getPermission() {
        return "superior.island.ratings";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "ratings";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_RATINGS.getMessage(locale);
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
        return IslandPrivileges.RATINGS_SHOW;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_RATINGS_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        plugin.getMenus().openIslandRatings(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()), island);
    }

}
