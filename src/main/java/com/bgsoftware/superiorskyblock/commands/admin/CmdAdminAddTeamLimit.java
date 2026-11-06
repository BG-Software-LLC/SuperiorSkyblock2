package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.NumberArgument;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.upgrade.IslandUpgradeConstants;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminAddTeamLimit implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("addteamlimit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.addteamlimit";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin addteamlimit <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_LIMIT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_ADD_TEAM_LIMIT.getMessage(locale);
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
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        NumberArgument<Integer> arguments = CommandArguments.getAdditionalLimit(sender, args[3]);

        if (!arguments.isSucceed()) {
            return;
        }

        int limit = arguments.getNumber();

        boolean isUnlimited = false;
        boolean isInvalid = false;
        Island changedIsland = null;
        int islandsChangedCount = 0;

        for (Island island : islands) {
            int currentLimit = island.getTeamLimit();
            if (currentLimit <= IslandUpgradeConstants.NO_LIMIT_VALUE) {
                isUnlimited = true;
                continue;
            }

            if (currentLimit + limit < 0) {
                isInvalid = true;
                continue;
            }

            PluginEvent<PluginEventArgs.IslandChangeMembersLimit> event = PluginEventsFactory.callIslandChangeMembersLimitEvent(
                    island, sender, currentLimit + limit);
            if (!event.isCancelled()) {
                island.setTeamLimit(event.getArgs().membersLimit);
                changedIsland = island;
                ++islandsChangedCount;
            }
        }

        if (islandsChangedCount <= 0) {
            if (isUnlimited) {
                Message.LIMIT_IS_UNLIMITED.send(sender);
            } else if (isInvalid) {
                Message.INVALID_LIMIT.send(sender, limit);
            }
            return;
        }

        if (islandsChangedCount > 1) {
            Message.CHANGED_TEAM_LIMIT_ALL.send(sender);
        } else if (targetPlayer == null) {
            Message.CHANGED_TEAM_LIMIT_NAME.send(sender, changedIsland.getName());
        } else {
            Message.CHANGED_TEAM_LIMIT.send(sender, targetPlayer.getName());
        }
    }

}
