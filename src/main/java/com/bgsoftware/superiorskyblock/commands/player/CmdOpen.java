package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.common.collections.Lists;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.BaseCommand;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;

import java.util.Arrays;
import java.util.List;

public class CmdOpen extends BaseCommand implements IPermissibleCommand {

    @Override
    protected List<String> aliases() {
        return Lists.newLinkedList("open", "unlock");
    }

    @Override
    protected String permission() {
        return "superior.island.open";
    }

    @Override
    protected String usage(java.util.Locale locale) {
        return "open";
    }

    @Override
    protected String description(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_OPEN.getMessage(locale);
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
    public Message getPermissionLackMessage() {
        return Message.NO_OPEN_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        if (plugin.getEventsBus().callIslandOpenEvent(island, superiorPlayer)) {
            island.setLocked(false);
            Message.ISLAND_OPENED.send(superiorPlayer);
        }
    }

}
