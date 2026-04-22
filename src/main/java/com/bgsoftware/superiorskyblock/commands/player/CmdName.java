package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandNames;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;

import java.util.Arrays;
import java.util.List;

public class CmdName implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("name", "setname", "rename");
    }

    @Override
    public String getPermission() {
        return "superior.island.name";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "name <" + Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_NAME.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.CHANGE_NAME;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_NAME_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        PluginEvent<PluginEventArgs.IslandRename> event = PluginEventsFactory.callIslandRenameEvent(island, superiorPlayer, args[1]);

        if (event.isCancelled())
            return;

        String islandName = event.getArgs().islandName;

        if (!IslandNames.isValidName(superiorPlayer, island, islandName))
            return;

        island.setName(islandName);

        String coloredName = island.getName();

        IslandNames.announceChange(island, Message.NAME_ANNOUNCEMENT, superiorPlayer.getName(), coloredName);
        Message.CHANGED_NAME.send(superiorPlayer, coloredName);
    }

}
