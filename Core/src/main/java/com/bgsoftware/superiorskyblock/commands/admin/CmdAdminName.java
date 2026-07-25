package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandNames;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class CmdAdminName implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("name", "setname", "rename");
    }

    @Override
    public String getPermission() {
        return "superior.admin.name";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin name <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_NAME.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 4;
    }

    @Override
    public int getMaxArgs() {
        return 4;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean supportMultipleIslands() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer targetPlayer, Island island, String[] args) {
        PluginEvent<PluginEventArgs.IslandRename> event = PluginEventsFactory.callIslandRenameEvent(island, sender, args[3]);

        if (event.isCancelled())
            return;

        String islandName = event.getArgs().islandName;

        if (!IslandNames.isValidName(sender, island, islandName))
            return;

        String oldName = island.getName();
        island.setName(islandName);

        String coloredName = island.getName();

        if (targetPlayer == null) {
            IslandNames.announceChange(island, Message.NAME_ANNOUNCEMENT_OTHER_NAME, sender.getName(), oldName, coloredName);
            Message.CHANGED_NAME_OTHER_NAME.send(sender, oldName, coloredName);
        } else {
            IslandNames.announceChange(island, Message.NAME_ANNOUNCEMENT_OTHER, sender.getName(), targetPlayer.getName(), coloredName);
            Message.CHANGED_NAME_OTHER.send(sender, targetPlayer.getName(), coloredName);
        }
    }

}
