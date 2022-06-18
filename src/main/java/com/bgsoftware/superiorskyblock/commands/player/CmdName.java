package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.island.IslandNames;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
        EventResult<String> eventResult = plugin.getEventsBus().callIslandRenameEvent(island, superiorPlayer, args[1]);

        if (eventResult.isCancelled())
            return;

        String islandName = eventResult.getResult();

        if (!IslandNames.isValidName(superiorPlayer, island, islandName))
            return;

        island.setName(islandName);

        String coloredName = plugin.getSettings().getIslandNames().isColorSupport() ?
                Formatters.COLOR_FORMATTER.format(islandName) : islandName;

        for (Player player : Bukkit.getOnlinePlayers())
            Message.NAME_ANNOUNCEMENT.send(player, superiorPlayer.getName(), coloredName);

        Message.CHANGED_NAME.send(superiorPlayer, coloredName);
    }

}
