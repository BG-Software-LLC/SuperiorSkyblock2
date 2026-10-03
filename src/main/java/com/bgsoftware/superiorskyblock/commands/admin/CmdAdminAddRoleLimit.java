package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.NumberArgument;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.upgrade.IslandUpgradeConstants;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminAddRoleLimit implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("addrolelimit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.addrolelimit";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin addrolelimit <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_ISLAND_ROLE.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_LIMIT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_ADD_ROLE_LIMIT.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 5;
    }

    @Override
    public int getMaxArgs() {
        return 5;
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
        PlayerRole playerRole = CommandArguments.getPlayerRoleForLimit(sender, args[3]);
        if (playerRole == null) {
            return;
        }

        NumberArgument<Integer> arguments = CommandArguments.getAdditionalLimit(sender, args[4]);

        if (!arguments.isSucceed()) {
            return;
        }

        int limit = arguments.getNumber();

        Island changedIsland = null;
        int islandsChangedCount = 0;

        for (Island island : islands) {
            int currentLimit = island.getRoleLimit(playerRole);
            if (currentLimit <= IslandUpgradeConstants.NO_LIMIT_VALUE) {
                continue;
            }

            PluginEvent<PluginEventArgs.IslandChangeRoleLimit> event = PluginEventsFactory.callIslandChangeRoleLimitEvent(
                    island, sender, playerRole, currentLimit + limit);
            if (!event.isCancelled()) {
                island.setRoleLimit(playerRole, event.getArgs().roleLimit);
                changedIsland = island;
                ++islandsChangedCount;
            }
        }

        if (islandsChangedCount <= 0) {
            Message.INVALID_LIMIT.send(sender, limit);
            return;
        }

        if (islandsChangedCount > 1) {
            Message.CHANGED_ROLE_LIMIT_ALL.send(sender, playerRole);
        } else if (targetPlayer == null) {
            Message.CHANGED_ROLE_LIMIT_NAME.send(sender, playerRole, changedIsland.getName());
        } else {
            Message.CHANGED_ROLE_LIMIT.send(sender, playerRole, targetPlayer.getName());
        }
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getPlayerRoles(plugin, args[3], IslandUtils::isValidRoleForLimit)
                : Collections.emptyList();
    }

}
