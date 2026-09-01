package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdAdminClose implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("close", "lock");
    }

    @Override
    public String getPermission() {
        return "superior.admin.close";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin close <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + ">";

    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_CLOSE.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean supportMultipleIslands() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        List<Island> changedIslands = new ArrayList<>();

        for (Island island : islands) {
            if (!island.isLocked() && PluginEventsFactory.callIslandCloseEvent(island, sender)) {
                changedIslands.add(island);
                island.setLocked(true);
            }
        }

        if (changedIslands.isEmpty()) {
            Message.ISLAND_ALREADY_CLOSED.send(sender);
            return;
        }

        if (changedIslands.size() > 1) {
            Message.CLOSED_ISLAND_ALL.send(sender);
        } else if (targetPlayer == null) {
            Message.CLOSED_ISLAND_NAME.send(sender, changedIslands.get(0).getName());
        } else {
            Message.CLOSED_ISLAND.send(sender, targetPlayer.getName());
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return args.length == 3 ? CommandTabCompletes.getOnlinePlayersAndMultipleIslands(plugin, args[2], false,
                (superiorPlayer, island) -> !island.isLocked()) : Collections.emptyList();
    }

}
