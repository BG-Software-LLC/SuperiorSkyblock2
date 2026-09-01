package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CmdAdminIgnore implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("ignore");
    }

    @Override
    public String getPermission() {
        return "superior.admin.ignore";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin ignore <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_IGNORE.getMessage(locale);
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
            if (!island.isIgnored()) {
                changedIslands.add(island);
                island.setIgnored(true);
            }
        }

        if (changedIslands.isEmpty()) {
            Message.ISLAND_ALREADY_IGNORED.send(sender);
            return;
        }

        if (changedIslands.size() > 1) {
            Message.IGNORED_ISLAND_ALL.send(sender);
        } else if (targetPlayer == null) {
            Message.IGNORED_ISLAND_NAME.send(sender, changedIslands.get(0).getName());
        } else {
            Message.IGNORED_ISLAND.send(sender, targetPlayer.getName());
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return args.length == 3 ? CommandTabCompletes.getOnlinePlayersAndMultipleIslands(plugin, args[2], false,
                (superiorPlayer, island) -> !island.isIgnored()) : Collections.emptyList();
    }

}
